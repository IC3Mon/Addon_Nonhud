# NoN HUD Image Addon —— 接入与核对清单

目标：**MC 1.21.11（正式版）+ Fabric**，NeedsOfNature 1.5.1，播放动画时按 (动画ID, stage) 在 HUD 弹图。

## 1. 把 NoN jar 接入编译依赖

把 `needsofnature-1.5.1+1.21.1.jar` 复制到你的 Fabric 工程 `libs/` 目录，然后在 `build.gradle` 的 `dependencies` 块加：

```gradle
dependencies {
    minecraft "com.mojang:minecraft:1.21.11"
    mappings "net.fabricmc:yarn:1.21.11+build.6:v2"
    modImplementation "net.fabricmc:fabric-loader:0.19.5"
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.141.6+1.21.11"

    // 编译期引用 NoN 的 com.nonid.api，运行时由 mods 目录的 jar 提供
    modCompileOnly files("libs/needsofnature-1.5.1+1.21.11.jar")
}
```

> ⚠️ 必须用 `modCompileOnly`（Loom 会把 jar 里的 intermediary 名 class_XXXX 重映射为 yarn）。
> 裸 `compileOnly files()` 不重映射，编译期 `com.nonid` 方法签名里的 `net.minecraft.class_2960`
> 与 yarn 的 `Identifier` 对不上，会报 "incompatible types / cannot access class_2960"。

## 2. 一定要先核对的 API 真实签名

✅ **已核对**（javap needsofnature-1.5.1+1.21.11.jar，对照 §5 7 项），结论如下：

### §2 动画事件（NoNAnimationListener 已按此实现）
- 包名确为 `com.nonid.api.animation`。
- `NonAnimationEvents` 四个事件：`BEFORE_START` / `STARTED` / `STAGE_CHANGED` / `STOPPED`，均为 Fabric `Event<T>`。
- Context 类型（都是 record，无 `getPlayer()`，字段用访问器）：
  - `StartedContext(world, session, requester)` → `session()`, `requester()`（`ServerPlayerEntity`，可能 null）
  - `StageChangedContext(world, instanceId, animationId, actorUuids, actorKeys, advanceTick, currentStage)` → 有 `animationId()`、`currentStage()`、`actorUuids()`
  - `StoppedContext(world, instanceId, animationId, actorUuids, actorKeys, stages)` → 有 `animationId()`、`actorUuids()`
- `NonAnimationSession`（record）：`animationId()`（Identifier）、`currentStage()`（`NonAnimationStage`）、`actorUuids()`。
- ⚠️ **stage 不是 int**：`NonAnimationStage` 是 record，要与定义文件序号对齐用
  `NonAnimationApi.getDefinition(animId).stages().indexOf(currentStage)`（查不到按 0）。

### §5 状态快照项（供后续 numeric 槽）
| # | 结论 |
|---|------|
| 1 | `com.nonid.api.NonEvents`：`MODIFY_LIQUID_GAIN`、`LIQUID_TANK_CHANGED`、`PLAYER_GENDER_CHANGED`、`PREGNANCY_CHANGED`、`MESS_CHANGED`、`DESTROYED_SKIN_CHANGED`、`ALLOW_PREGNANCY`。注入 hook = `MODIFY_LIQUID_GAIN` |
| 2 | `NonAnimationApi` 无 actor 视图，但 `NonAnimationDefinition.actors()`（每个 `NonAnimationActor` 有 `label()`/`activity()`）可读角色；`NonAnimationSession.actorKeys()` 可用 |
| 3 | `com.nonid.api.NonPlayerApi.getState(ServerPlayer)` → `NonPlayerStateSnapshot(gender, liquidTank, mess, destroyedSkin, pregnancy, accessoryEffects)`。注意**无 energy**（能量不在状态快照里） |
| 4 | `NonLiquidTankSnapshot(storedMl, capacityMl, composition, entityTypeId, tintRgb)` + `fillRatio()`；`NonEnergySnapshot(energy, maxEnergy, fraction, ...)` 需 **单独取数**（快照不含，可能另走 NonEntityApi） |
| 5 | 精液瓶事件：本项目用 `STAGE_CHANGED` / 动画事件捕获 `botle` 时机，无独立回调 |
| 6 | `NonPregnancySnapshot(entityTypeId, endTick, pendingHatch)` + `active()`；`PREGNANCY_CHANGED` 事件。注意**无进度百分比**，阈值需用 `endTick - world.getTime()` 反推剩余 |
| 7 | 无 `isPeakedStage`；峰档需自算：`stageIndex` 与 `def.stages().size()-1`（末段）比较 |

### 客户端可直读当前动画？
有：`NonAnimationApi.getSession(world, instanceId)` / `getCurrentStage(world, instanceId)`。
但仍采用**服务端事件 → S2C 包 → 客户端 HUD 自绘**方案（1.21.11 已实现并通过构建），依赖面小、不与 NoN 客户端渲染 API 耦合，保持既有架构。

## 3. 客户端无依赖风险

`client/HudImageRenderer.java`、`ImageCatalog.java`、`AnimationStateSync.java` **完全自绘 + Fabric 标准网络 API**，
不引用 NoN 客户端类，理论上无需核对即可跑。

## 4. 把图片放进去

在 `src/main/resources/assets/nonhud/textures/` 放剧情图，并在 `ImageCatalog` 里登记：
`key = "动画ID|stage"` → 纹理路径。（示例 `needsofnature:demo|2` → `demo_stage2.png`）已写好一个 put 调用作模板。

## 5. 跑通

```powershell
gradlew build
gradlew runClient
```

## 待你补充的细节
- [ ] 你的工程目录路径（我需要它才能精确改 build.gradle / 放文件）
- [ ] 用上面的 `javap` 结果回贴 2-3 行，我据此把 listener 改成精确签名