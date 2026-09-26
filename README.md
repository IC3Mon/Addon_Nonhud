# NeedsOfNature Customization Guide and Pack Creation Guide

This document describes the current customization and pack formats for NeedsOfNature.

Pack-facing names such as the `animationframework` resource namespace, `afw_animdefs` directory, and `afw_*` model keys remain the stable NoN content format.

It covers both local player-side customization, such as ripped skin texture overrides, and NoN pack authoring for animations, models, accessory items, liquid values, recipes, and visual overlays without changing Java code.

<a id="table-of-contents"></a>
## Table of Contents

**[Ripped Skin & Masks](#ripped-skin-masks)**

- [Ripped Player Textures](#ripped-player-textures)
- [Male Model D Texture Area](#male-model-d-texture-area)
- [Ripped Stage Masks](#ripped-stage-masks)
- [Quick Skin and Skin Overrides Compatibility](#quick-skin-and-skin-overrides)
- [Per-Skin Ripped Textures and Masks](#per-skin-ripped-textures-and-masks)

**[Custom Player Models (CPM)](#custom-player-models)**

- [CPM Texture Compatibility](#cpm-texture-compatibility)
- [Creating a Custom Atlas Profile](#creating-cpm-custom-atlas-profile)
- [Per-Skin CPM Profiles](#per-skin-cpm-profiles)
- [CPM Split Arms and Legs](#cpm-split-arms-and-legs)
- [CPM Value Layers](#cpm-value-layers)
- [CPM and Female Gender Mod](#cpm-and-female-gender-mod)

**[Figura / NoFigura](#figura)**

- [Basic Figura Compatibility](#basic-figura-compatibility)
- [Generating a Figura Profile](#generating-figura-profile)
- [Figura `profile.json`](#figura-profile-json)
- [Figura Animation Pose Compatibility](#figura-animation-pose-compatibility)
- [Figura Split Arms and Legs](#figura-split-arms-and-legs)
- [Figura Lua Values](#figura-lua-values)
- [Figura and Female Gender Mod](#figura-and-female-gender-mod)

**[Pack Creation](#pack-creation)**

*[Pack Quality Guidelines](#pack-convention-and-quality-guidelines)*
- [Keep Pre-Peak Attack Stages Escapable](#pre-peak-attack-stage-escapability)
- [Reuse Unchanged Shared Models](#reuse-unchanged-shared-models)
- [Keep Multi-Stage Animations Concise](#keep-multi-stage-animations-concise)
- [Provide Previews for Player x Player Animations](#player-x-player-animation-previews)

*[Pack Structure & Metadata](#pack-structure-and-metadata)*
- [Pack Location](#pack-location)
- [Empty Pack Structure Example](#empty-pack-structure-example)
- [Minimal `pack.mcmeta`](#pack-mcmeta)

*[Authoring & Testing Tools](#authoring-and-testing-tools)*
- [AnimationDirector Blockbench Plugin](#animationdirector-blockbench-plugin)
- [Debug Staff](#debug-staff)

*[Models & Rendering](#models-and-rendering)*
- [Local and Imported GeckoLib Models](#local-and-imported-geckolib-models)
- [Default Model Revisions](#default-model-revisions)
- [GeckoLib Model Render Metadata](#geckolib-model-render-metadata)
- [Full Model Texture](#full-model-texture)
- [Bone Texture Overrides](#bone-texture-overrides)
- [Emissive Textures](#emissive-textures)
- [Render Settings](#render-settings)
- [CEM/JEM Model Assets](#cem-jem-model-assets)

*[Animation Definitions (AnimDefs)](#animation-definitions)*
- [Animation Definition Files](#animation-definition-files)
- [Full Animdef Skeleton](#full-animdef-skeleton)
- [Top-Level Animdef Keys](#top-level-animdef-keys)
- [Camera Tracking](#camera-tracking)
- [Actor Keys](#actor-keys)
- [Actor Constraint Keys](#actor-constraint-keys)
- [Injector Roles](#injector-roles)
- [Actor Activity](#actor-activity)
- [System Tags](#system-tags)
- [Content Tags](#content-tags)
- [Attack Eligibility](#attack-eligibility)
- [Matching and Weights](#matching-and-weights)
- [Join Chains](#join-chains)
- [Stages](#stages)
- [Stage Props](#stage-props)
- [Held-Item Requirements and Dynamic Props](#held-item-requirements-and-dynamic-props)
- [Manual Peak Held-Item Rules](#manual-peak-held-item-rules)
- [Tease Animations](#tease-animations)
- [Stuck Animations](#stuck-animations)
- [Defeated Animations](#defeated-animations)

*[Animation Assets & Timeline Events](#animation-assets-and-timeline-events)*
- [GeckoLib Animation Assets](#geckolib-animation-assets)
- [Animation Previews](#animation-previews)
- [Creating an Animation Preview in Blockbench](#creating-animation-preview)
- [Reactive Impact Cues](#reactive-impact-cues)
- [Other Special Sound Cues](#other-special-sound-cues)
- [Normal NoN Sound Cues](#normal-non-sound-cues)
- [Inline Random Sound Pools](#inline-random-sound-pools)
- [Particle Keyframes](#particle-keyframes)

*[Placement Requirements](#placement-requirements)*
- [Wall Block Requirements](#wall-block-requirements)
- [Example: Low Wall Requirement](#low-wall-requirement-example)
- [Example: Two-Wide Fence Requirement](#two-wide-fence-requirement-example)
- [Center Support Block Requirements](#center-support-block-requirements)
- [Directional Clearance Center Support](#directional-clearance-center-support)
- [Surface Footprint Center Support](#surface-footprint-center-support)
- [Water Requirements](#water-requirements)

*[Entity Profiles](#entity-profiles)*
- [Custom Pregnancy Egg Models](#custom-pregnancy-egg-models)

**[Accessories](#accessories)**

- [Data-Driven Accessories](#data-driven-accessories)
- [Accessory Item Keys](#accessory-item-keys)
- [Accessory Enchantments](#accessory-enchantments)
- [Accessory Effects](#accessory-effects)
- [Accessory Skin Overlays](#accessory-skin-overlays)
- [Animation-Blocking Accessory Example](#animation-blocking-accessory-example)
- [Recipes and Advancements](#recipes-and-advancements)

**[Addon API](#addon-api)**

- [Player State](#player-state)
- [Animation Queries and Control](#animation-queries-and-control)
- [Player Interaction Profiles](#player-interaction-profiles)
- [Client Animation Hooks](#client-animation-hooks)

---

<a id="ripped-skin-masks"></a>
## Ripped Skin & Masks

This chapter is written from the client/player point of view. It is for overriding your own ripped player texture and stage masks in your own Minecraft instance, not for creating a content pack.

<a id="ripped-player-textures"></a>
### Ripped Player Textures

To override your own ripped player texture, place this file in:

```text
<minecraft instance>/config/needsofnature/destroyed_skin.png
```

Use a static, square player-skin layout PNG from 64x64 through 512x512 in 64-pixel steps. HD ripped skins retain their detail even with a normal 64x64 player skin and do not require an HD-skin mod. This file is the highest-priority player-side ripped skin override. If it exists, NoN uses it directly and ignores the generated tinted fallback and default-skin-specific ripped textures for your player.

Stage 1-3 blend this texture onto your normal skin through the ripped stage masks. Stage 4 uses the full `destroyed_skin.png` texture directly.

See `Tutorial/ripped skin examples/` for example texture files you can use as reference.

This local config file is uploaded and synced from the player who owns it. If multiple players want custom ripped textures, every player needs to provide their own files in their own `config/needsofnature/` directory.

<a id="male-model-d-texture-area"></a>
### Male Model D Texture Area

The male player model uses the otherwise unused strip at `x=56-63`, `y=16-47` on a standard 64x64 player skin to texture its D bones. When creating `destroyed_skin.png` for a male or male+female player, texture this area as well.

The highlighted strip shows the section used by the male model's D bones.

![Male model D texture area](img/ripped_skin_d_texture_area.png)

<br>

<a id="ripped-stage-masks"></a>
### Ripped Stage Masks

Optional local stage mask overrides use:

```text
<minecraft instance>/config/needsofnature/destroyed_skin_mask_1.png
<minecraft instance>/config/needsofnature/destroyed_skin_mask_2.png
<minecraft instance>/config/needsofnature/destroyed_skin_mask_3.png
```

Masks are 64x64 grayscale/alpha-style PNGs.

White/opaque pixels apply the ripped texture for that stage. Black/transparent pixels keep the original skin. Stage 4 does not use a mask; it uses the full ripped texture.

<a id="quick-skin-and-skin-overrides"></a>
### Quick Skin and Skin Overrides Compatibility

When Quick Skin or Skin Overrides is installed, NoN uses the currently selected static skin as the player's base skin. Ripped stages, mess and liquid overlays, and accessory overlays are applied to that skin both normally and during NoN animations. Standard-resolution and HD static skins are supported. Animated skins are not currently supported.

Changing the selected Quick Skin refreshes NoN's generated player textures automatically. Skin Overrides is also supported, but changing to a named per-skin override may currently require leaving and rejoining the world before NoN applies its matching ripped texture and masks.

<a id="per-skin-ripped-textures-and-masks"></a>
#### Per-Skin Ripped Textures and Masks

NoN can select different ripped textures and masks for skins selected through Quick Skin or Skin Overrides using the skin's display name. The name is converted to lowercase, spaces and unsafe path characters become underscores, and repeated separators are collapsed. For example, a skin named `My Skin` uses the folder `my_skin`:

```text
<minecraft instance>/config/needsofnature/my_skin/destroyed_skin.png
<minecraft instance>/config/needsofnature/my_skin/destroyed_skin_mask_1.png
<minecraft instance>/config/needsofnature/my_skin/destroyed_skin_mask_2.png
<minecraft instance>/config/needsofnature/my_skin/destroyed_skin_mask_3.png
```

These folders are local player configuration, not NoN pack content. Each file falls back independently. For an active Quick Skin or Skin Overrides skin, NoN uses this priority:

1. The matching file in the skin-named folder.
2. The corresponding global file directly inside `config/needsofnature/`.
3. NoN's normal generated/default ripped skin or bundled stage mask.

If neither supported skin mod has a skin selected, NoN skips the skin-named folder and uses the global files and normal fallback behavior.

Changing or renaming the selected skin changes which ripped texture and masks NoN resolves. The folder is based on the display name, not the skin mod's internal asset id, so renaming a skin also changes the folder NoN looks for. Other players do not need Quick Skin, Skin Overrides, or copies of these files; NoN synchronizes the resolved images through the server.

CPM and Figura models with custom texture atlases use their own atlas-shaped profiles. See the dedicated Custom Player Models and Figura / NoFigura chapters below.

When both integrations are available, an active Figura avatar takes priority. CPM is used when no Figura avatar is active.

---

<a id="custom-player-models"></a>
## Custom Player Models (CPM)

NoN supports Customizable Player Models as an optional client mod. When a player has a loaded CPM model, NoN renders that model during GeckoLib animations. Both exported `.cpmmodel` files and projects currently open as `.cpmproject` files are supported.

Geometry attached to CPM's normal head, body, arm, and leg roots follows the corresponding NoN animation bones automatically. Models may add geometry to the vanilla body or replace vanilla body parts completely. Models with separately bending forearms and lower legs require the split-limb setup described below.

<a id="cpm-texture-compatibility"></a>
### CPM Texture Compatibility

CPM models that use the normal Minecraft player skin automatically receive NoN's regular ripped skin, mess, liquid-tank, and accessory skin overlays. This works both outside and inside NoN animations and does not require a separate CPM texture profile.

Models with a custom texture atlas need profile images matching that atlas exactly. NoN cannot infer which parts of an arbitrary atlas represent skin, clothing, or each mess region, so it leaves a custom atlas unchanged until a matching profile is provided.

<a id="creating-cpm-custom-atlas-profile"></a>
#### Creating a Custom Atlas Profile

NoN can extract CPM's final stitched atlas and create correctly sized templates:

1. Select the CPM model and enter a world so CPM can finish loading it.
2. Open `NoN Settings -> Player Preferences -> Player Skin/Model -> Custom Player Models mod settings`.
3. Open `CPM texture profile` and press `Create missing templates`.
4. Edit the generated files in the folder NoN opens.

The profile is stored under a sanitized version of the CPM model or project name:

```text
<minecraft instance>/config/needsofnature/cpm/<model_name>/
|-- reference_texture.png
|-- destroyed_skin.png
|-- destroyed_skin_mask_1.png
|-- destroyed_skin_mask_2.png
|-- destroyed_skin_mask_3.png
|-- README.txt
`-- mess/
    |-- v_mess1.png
    |-- v_mess2.png
    |-- v_mess3.png
    |-- a_mess1.png
    |-- a_mess2.png
    |-- a_mess3.png
    |-- m_mess1.png
    |-- m_mess2.png
    |-- m_mess3.png
    |-- v_mess_tank.png
    `-- a_mess_tank.png
```

`reference_texture.png` is an untouched copy of CPM's final stitched atlas and is not loaded by NoN. Use it to identify the model's UV regions. `destroyed_skin.png` begins as the same atlas and should be edited into its fully ripped appearance.

The generated masks copy the reference atlas's transparency and turn every visible pixel black. This preserves the complete UV silhouette while remaining visually inactive. Paint areas lighter or white to enable an effect there; black and fully transparent pixels do nothing.

The three destroyed masks control where `destroyed_skin.png` appears during ripped stages 1-3. Mess and tank images are tint masks. NoN applies the relevant liquid color at runtime wherever those masks are painted lighter.

Every runtime profile image must use exactly the same width and height as `reference_texture.png`. Do not crop, scale, or rearrange the atlas. Dimensions from `64x64` through `2048x2048` are accepted. Each PNG may be at most 1 MiB and the complete uploaded profile may be at most 8 MiB.

Creating templates again only adds missing files and never overwrites edited runtime textures. `Refresh reference` replaces only `reference_texture.png`. Open CPM projects use their `.cpmproject` name when determining the profile folder.

The owning client uploads its active profile through NoN and the server synchronizes it to other players. Other players do not need copies of the profile files. Custom-atlas profiles currently cover ripped skin, mess, and liquid-tank masks; skin-layout accessory overlays cannot automatically be relocated onto an arbitrary custom UV atlas.

<a id="per-skin-cpm-profiles"></a>
#### Per-Skin CPM Profiles

When Quick Skin or Skin Overrides selects a named skin, a CPM model can use skin-specific profile files. Place only the files that differ inside:

```text
<minecraft instance>/config/needsofnature/cpm/<model_name>/<skin_name>/
```

The skin name uses the same sanitized folder convention described under Per-Skin Ripped Textures and Masks. Each asset falls back independently to the model's general profile file when no skin-specific version exists.

<a id="cpm-split-arms-and-legs"></a>
### CPM Split Arms and Legs

The NoN player rig has independently animated elbow and knee sections. A CPM model without split-limb markers still follows the upper arm and leg poses, but its lower segments cannot receive the separate elbow and knee motion.

Prepare up to four CPM groups with these exact names:

| CPM group        | NoN animation section |
| ---------------- | --------------------- |
| `rightarm_elbow` | Right forearm         |
| `leftarm_elbow`  | Left forearm          |
| `rightleg_knee`  | Right lower leg       |
| `leftleg_knee`   | Left lower leg        |

Each lower segment should be a child of its corresponding upper limb. Place its pivot at the elbow or knee and put the lower-limb geometry inside that group.

With NoN and CPM installed, open the model in CPM's editor and run `Generate NoN Split-Limb Markers`. The button creates hidden marker animations that let NoN resolve the selected CPM elements after export. The markers do not add visible geometry or play as normal animations.

Run the generator again after replacing or reorganizing a marked group. It removes the previous generated markers before creating new ones. Missing groups are reported and simply remain unsupported; duplicate groups with the same required name stop generation because NoN could not identify one unambiguous target.

<a id="cpm-value-layers"></a>
### CPM Value Layers

NoN exposes player state through named CPM Custom Value Layers. Create a Value Layer in the CPM editor, give it one of the exact names below, set its maximum value to the listed maximum, and enable `Command Activated Animation`. NoN updates only layers present in the selected model.

The `Max value` setting is easy to miss: open CPM's `Animation` tab, select the Value Layer, then click `Layer Default Value` near the bottom of the animation controls. The popup contains `Max value` and `Interpolate Value`. `Layer Default Value` is only usable while a layer animation is selected.

| Value layer                        | Maximum | Meaning                                                                                                            |
| ---------------------------------- | -------:| ------------------------------------------------------------------------------------------------------------------ |
| `needsofnature_energy`             | `200`   | Current player energy.                                                                                             |
| `needsofnature_ripped_stage`       | `10`    | Current ripped-skin damage.                                                                                        |
| `needsofnature_pregnancy_progress` | `255`   | Increases from conception toward birth.                                                                            |
| `needsofnature_liquid_tank_fill`   | `100`   | Liquid tank fill percentage.                                                                                       |
| `needsofnature_v_mess`             | `10`    | Current V mess.                                                                                                    |
| `needsofnature_a_mess`             | `10`    | Current A mess.                                                                                                    |
| `needsofnature_m_mess`             | `10`    | Current M mess.                                                                                                    |
| `needsofnature_peak_progress`      | `255`   | Time-weighted progress across every stage before the peaked stage. It remains `255` after the peaked stage begins. |
| `needsofnature_is_peaked_stage`    | `1`     | `1` only while the current stage is the peaked stage.                                                              |
| `needsofnature_struggle_progress`  | `255`   | Normalized escape or player-versus-player mash progress.                                                           |
| `needsofnature_is_struggling`      | `1`     | `1` after valid alternating A/D input and returns to `0` after one second without input.                           |
| `needsofnature_give_up_progress`   | `255`   | Normalized progress while giving up.                                                                               |

Boolean layers use `0` for false and `1` for true. Values that do not currently apply return `0`. If a layer has a lower maximum than listed, NoN clamps the value to that smaller maximum and the model cannot represent the complete range.

CPM does not redistribute the configured numeric range across the available frames. Values address the Value Layer's frames directly: if `Max value` is `10` but the layer contains only three frames, it reacts to values `1` through `3`, not values `4` through `10`. Add enough frames for the values the model needs to represent; increasing `Max value` alone does not stretch a short frame sequence across the larger range. `Interpolate Value` can smooth transitions between neighboring authored frames.

CPM does not provide direct expressions that combine multiple Value Layers. To show geometry only when two conditions are true, place it inside two nested groups and let each Value Layer control one group's visibility. A hidden parent also hides its child group.

<a id="cpm-and-female-gender-mod"></a>
### CPM and Female Gender Mod

`Hide FGM breasts for replaced CPM torso` is available in the CPM settings and defaults to enabled. It hides FGM breasts while the active CPM model replaces the player's torso.

---

<a id="figura"></a>
## Figura / NoFigura

NoN supports Figura and compatible ports such as NoFigura. The integration renders the active avatar during NoN animations, exposes NoN player values to Lua, supports custom ripped and mess atlases, and can retarget humanoid avatar parts to NoN's animated player rig.

NoN does not distribute Figura avatars itself. Avatar loading and sharing continue to use Figura/NoFigura's normal systems. The NoN profile and texture masks described below live inside the avatar so they can travel with it as avatar resources.

<a id="basic-figura-compatibility"></a>
### Basic Figura Compatibility

Avatar geometry attached to Figura's standard vanilla head, body, arm, and leg parent types follows NoN's animated player model. A simple avatar that only adds a hat, tail, backpack, or similar geometry often works without additional mappings.

NoN respects which vanilla body parts the avatar hides. Figura replacement arms, legs, bodies, and heads take the place of the corresponding NoN player parts during animations.

Scripts, render events, colors, texture selection, visibility, and other avatar state continue to update. Scripted transforms can conflict with NoN's animation transforms on more complicated avatars; use Animation Pose Compatibility for those models.

<a id="generating-figura-profile"></a>
### Generating a Figura Profile

Custom Figura atlases need masks matching their own UV layouts. NoN can generate an avatar-local profile and templates:

1. Keep the avatar unpacked as a folder; the generator cannot edit a packed avatar archive.
2. Select the avatar and enter a world so Figura finishes loading it.
3. Open `NoN Settings -> Player Preferences -> Player Skin/Model -> Figura / NoFigura settings`.
4. Press `Generate / update Figura profile` and edit the generated files.

The generator creates this structure inside the selected avatar:

```text
<figura avatar>/
|-- avatar.json
`-- needsofnature/
    |-- profile.json
    |-- README.txt
    `-- textures/
        `-- <atlas_name>/
            |-- reference_texture.png
            |-- destroyed_skin.png
            |-- destroyed_skin_mask_1.png
            |-- destroyed_skin_mask_2.png
            |-- destroyed_skin_mask_3.png
            `-- mess/
                |-- v_mess1.png
                |-- v_mess2.png
                |-- v_mess3.png
                |-- a_mess1.png
                |-- a_mess2.png
                |-- a_mess3.png
                |-- m_mess1.png
                |-- m_mess2.png
                |-- m_mess3.png
                |-- v_mess_tank.png
                `-- a_mess_tank.png
```

Each primary Figura atlas receives its own directory and `atlases` entry. `reference_texture.png` is an editing reference, `destroyed_skin.png` is the fully ripped atlas, the three destroyed-skin masks control where that texture appears during ripped stages 1-3, and the mess files are grayscale/alpha tint masks. Keep every image at the exact dimensions and UV layout of its corresponding reference atlas.

The generator also adds `needsofnature/**` to the existing `resources` array in `avatar.json`. If creating a profile manually, preserve existing avatar metadata and include the same resource pattern:

```json
{
  "resources": [
    "needsofnature/**"
  ]
}
```

<a id="figura-profile-json"></a>
### Figura `profile.json`

A complete profile can contain the following entries:

```json
{
  "format": 1,
  "render_non_d_bones": true,
  "rig": {
    "right_forearm": "right_forearm",
    "left_forearm": "left_forearm",
    "right_lower_leg": "right_lower_leg",
    "left_lower_leg": "left_lower_leg"
  },
  "atlases": [
    {
      "texture": "avatar_texture",
      "directory": "needsofnature/textures/avatar_texture"
    }
  ],
  "animation_pose": {
    "mode": "humanoid_override",
    "parts": {
      "head": "models.Avatar.root.Head",
      "waist": "models.Avatar.root.Body",
      "body": "models.Avatar.root.Body.UpperBody",
      "right_arm": "models.Avatar.root.RightArm",
      "left_arm": "models.Avatar.root.LeftArm",
      "right_leg": "models.Avatar.root.RightLeg",
      "left_leg": "models.Avatar.root.LeftLeg"
    }
  }
}
```

| Entry                | Required    | Meaning                                                                                                                                                                    |
| -------------------- | ----------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `format`             | Recommended | Profile format version. Use `1`.                                                                                                                                           |
| `render_non_d_bones` | No          | Defaults to `true`, allowing the NoN GeckoLib model's `d` and `d1` bones to render. Set it to `false` only when the Figura avatar supplies its own corresponding geometry. |
| `rig`                | No          | Maps NoN's four lower-limb roles to existing Figura part names. Default names are shown above.                                                                             |
| `atlases`            | No          | Connects a Figura texture name to its NoN profile directory. The generator fills this automatically.                                                                       |
| `animation_pose`     | No          | Configures humanoid animation-pose retargeting and optional explicit part mappings.                                                                                        |

The `directory` of every atlas must remain inside the avatar's `needsofnature/` directory. `reference_texture.png` is not used at runtime and does not need an entry in `profile.json`.

<a id="figura-animation-pose-compatibility"></a>
### Figura Animation Pose Compatibility

Animation Pose Compatibility helps scripted or unusually structured humanoid avatars follow NoN animation poses instead of retaining incompatible standing transforms.

Players can enable or disable this in `Figura / NoFigura settings`. The choice is saved separately for each avatar. Avatar creators can make it the default by adding:

```json
{
  "animation_pose": {
    "mode": "humanoid_override"
  }
}
```

NoN first tries Figura's standard vanilla parent types and common humanoid part names. Explicit `parts` mappings are only needed when automatic detection selects the wrong groups or cannot find them.

| Mapping role | Required | Common purpose                                         |
| ------------ | -------- | ------------------------------------------------------ |
| `head`       | Yes      | Head and geometry parented to it.                      |
| `body`       | Yes      | Main or upper torso.                                   |
| `waist`      | No       | Pelvis or lower-torso parent used by split-torso rigs. |
| `right_arm`  | Yes      | Complete right upper-arm hierarchy.                    |
| `left_arm`   | Yes      | Complete left upper-arm hierarchy.                     |
| `right_leg`  | Yes      | Complete right leg hierarchy.                          |
| `left_leg`   | Yes      | Complete left leg hierarchy.                           |

Mappings accept slash paths such as `root/Body/UpperBody` and paths copied from Figura such as `models.Avatar.root.Body.UpperBody`. A unique part name can also be used by itself. Matching is case-insensitive after an exact match is attempted.

For a split-torso avatar, map `waist` to the pelvis or lower-torso group that moves the whole upper body, then map `body` to its nested upper-torso child:

```json
{
  "animation_pose": {
    "mode": "humanoid_override",
    "parts": {
      "waist": "models.Avatar.root.LowerTorso",
      "body": "models.Avatar.root.LowerTorso.UpperTorso"
    }
  }
}
```

The remaining required roles may still be detected automatically. Explicit mappings do not require renaming the actual avatar parts.

Compatibility is still based on a humanoid skeleton. Avatars with non-humanoid proportions, unrelated pivots, disconnected body hierarchies, or scripts that reconstruct the body every frame may not align correctly. Leave Animation Pose Compatibility disabled when the avatar's own vanilla-parent setup already follows the animation correctly or when retargeting makes the result worse.

<a id="figura-split-arms-and-legs"></a>
### Figura Split Arms and Legs

Use the `rig` object to map NoN's independently animated lower-arm and lower-leg roles to the corresponding Figura parts:

```json
{
  "rig": {
    "right_forearm": "RightArmLower",
    "left_forearm": "LeftArmLower",
    "right_lower_leg": "RightLegLower",
    "left_lower_leg": "LeftLegLower"
  }
}
```

Matching is case-insensitive. Each mapped part should be a child of its corresponding upper limb, use an elbow or knee pivot, and contain the geometry that should receive the lower-limb motion.

The `rig` values currently match part names rather than full paths. Keep each mapped name unique; if multiple parts use the same mapped name, all matching parts receive the transform. Omitted entries fall back to `right_forearm`, `left_forearm`, `right_lower_leg`, and `left_lower_leg` respectively.

<a id="figura-lua-values"></a>
### Figura Lua Values

NoN adds a Figura Lua API named `needsofnature`. Its methods read the state of the player who owns the avatar, including remote players. NoN synchronizes the relevant state and transient animation input so other players with NoN and Figura/NoFigura can see scripted reactions.

| Lua call                               | Return range | Meaning                                                                            |
| -------------------------------------- | ------------:| ---------------------------------------------------------------------------------- |
| `needsofnature:getEnergy()`            | `0-200`      | Current player energy.                                                             |
| `needsofnature:getRippedStage()`       | `0-10`       | Current ripped-skin damage.                                                        |
| `needsofnature:getPregnancyProgress()` | `0-255`      | Increases from conception toward birth.                                            |
| `needsofnature:getLiquidTankFill()`    | `0-100`      | Liquid tank fill percentage.                                                       |
| `needsofnature:getVMess()`             | `0-10`       | Current V mess.                                                                    |
| `needsofnature:getAMess()`             | `0-10`       | Current A mess.                                                                    |
| `needsofnature:getMMess()`             | `0-10`       | Current M mess.                                                                    |
| `needsofnature:getPeakProgress()`      | `0-255`      | Time-weighted progress toward the peaked stage; remains `255` afterward.           |
| `needsofnature:isPeakedStage()`        | boolean      | `true` only during the peaked stage.                                               |
| `needsofnature:getStruggleProgress()`  | `0-255`      | Normalized escape or player-versus-player mash progress.                           |
| `needsofnature:isStruggling()`         | boolean      | `true` after valid alternating A/D input and false after one second without input. |
| `needsofnature:getGiveUpProgress()`    | `0-255`      | Normalized give-up progress.                                                       |

Values that do not currently apply return `0` or `false`. Guard the API lookup if the avatar should also run for players without NoN:

```lua
function events.tick()
    if not needsofnature then
        return
    end

    local energized = needsofnature:getEnergy() >= 70
    models.Avatar.root.EnergyIndicator:setVisible(energized)
end
```

<a id="figura-and-female-gender-mod"></a>
### Figura and Female Gender Mod

`Hide FGM breasts for replaced torso` is available in the Figura settings. It hides FGM breasts while the active Figura avatar replaces the player's torso.

---

<a id="pack-creation"></a>
## Pack Creation

<a id="pack-convention-and-quality-guidelines"></a>
### Pack Quality Guidelines

The following conventions are recommendations rather than parser requirements. Following them makes packs fairer to play, easier to combine, and less likely to break when NoN's shared assets change.

<a id="pre-peak-attack-stage-escapability"></a>
#### Keep Pre-Peak Attack Stages Escapable

For animations that can start as attacks, every stage before the first peak stage should remain escapable. Only the peak stage and stages after it should normally use `"escapable": false`. This gives the attacked player a fair opportunity to escape before the animation reaches its outcome.

Set the top-level default to `true`, then disable escape only where necessary:

```json
{
  "escapable": true,
  "stages": [
    {
      "stage": 1,
      "loop": true
    },
    {
      "stage": 2,
      "loop": true
    },
    {
      "stage": 3,
      "loop": true,
      "non_peak": true,
      "escapable": false
    },
    {
      "stage": 4,
      "loop": false,
      "escapable": false
    }
  ]
}
```

In this example, stage 3 is the first peak stage. Stages 1 and 2 inherit the escapable top-level default, while the peak and its outro cannot be escaped.

<a id="reuse-unchanged-shared-models"></a>
#### Reuse Unchanged Shared Models

Do not copy unchanged models from the default NoN pack into another pack. A copied model becomes a frozen duplicate and can become incompatible when the default model gains new bones, locators, render metadata, or fixes.

Every animation pack can provide local models. If a local model is absent, NoN uses an explicitly imported model, a compatible archived default model when required, and then the current shared model. Animation-only packs should simply omit unchanged models.

On resource reload, NoN compares local models and shared overrides against the complete model JSON from the current default pack. A formatting-only copy triggers a setup warning because keeping that duplicate is unnecessary and may cause incompatibilities after the default model is updated.

If an animation intentionally depends on another pack's custom model, import only the required entity family instead of copying its files:

```json
{
  "animationframework": {
    "id": "creator:my_pack",
    "model_imports": [
      {
        "pack": "other_creator:model_pack",
        "entities": [
          "minecraft:wolf"
        ]
      }
    ]
  }
}
```

Only provide a local model when the animation intentionally changes its geometry or requires a different bone structure. See [Local and Imported GeckoLib Models](#local-and-imported-geckolib-models) for the complete lookup rules.

<a id="keep-multi-stage-animations-concise"></a>
#### Keep Multi-Stage Animations Concise

If an ordinary mob animation has several stages before its peak, use `stage_duration_multiplier` so those stages share approximately one normal stage's total duration instead of each lasting the full configured duration.

For three looping pre-peak stages, a good starting point is:

```json
{
  "stages": [
    {
      "stage": 1,
      "loop": true,
      "stage_duration_multiplier": 0.3333
    },
    {
      "stage": 2,
      "loop": true,
      "stage_duration_multiplier": 0.3333
    },
    {
      "stage": 3,
      "loop": true,
      "stage_duration_multiplier": 0.3333
    },
    {
      "stage": 4,
      "loop": true,
      "non_peak": true
    }
  ]
}
```

Two pre-peak stages can generally start at `0.5` each, while four can start at `0.25` each. Final durations may shift slightly to finish a complete animation cycle. Player x player animations and special scripted animations may intentionally use longer pacing when that better fits their interaction.

<a id="player-x-player-animation-previews"></a>
#### Provide Previews for Player x Player Animations

Every player x player animation should include a clear preview image. Players use these images when selecting an animation and reviewing a request, so missing previews make otherwise polished packs harder to navigate. Follow the [Animation Previews](#animation-previews) tutorial for the Blockbench workflow and required resource path.

<a id="pack-structure-and-metadata"></a>
### Pack Structure & Metadata

<a id="pack-location"></a>
#### Pack Location

NoN loads external packs from the game directory:

```text
<minecraft instance>/needsofnature/
```

Both folders and `.zip` files are supported. Each pack uses normal Minecraft resource/data pack structure:

```text
needsofnature/
  MyPack/
    pack.mcmeta
    pack.png
    data/
      animationframework/
        afw_animdefs/
          my_animation.json
      needsofnature/
        non_entity_profiles/
          my_entity_profiles.json
    assets/
      animationframework/
        geckolib/
          animations/
            afw/
              my_animation.animation.json
      needsofnature/
        non_accessory_items/
          my_accessory.json
```

<a id="empty-pack-structure-example"></a>
#### Empty Pack Structure Example

One copyable [`example_packs/example_pack`](example_packs/example_pack/README.txt) template is included in the guide. It contains the basic animdef and conjoined-animation folders together with optional placeholders for both ID-derived local models and intentional shared model overrides.

The template is intentionally minimal. Replace the `example` namespace, pack ID, and metadata before use, then delete each placeholder as the corresponding real files are added. Most packs should use the local model folder only for geometry that intentionally differs from the shared model and should remove the shared-model placeholder.

Use `/reload` for server-data changes such as animdefs and liquid gains. Restart the game for data-driven accessory item registration, because new item registry entries must exist during startup.

<a id="pack-mcmeta"></a>
#### Minimal `pack.mcmeta`

```json
{
  "pack": {
    "pack_format": 64,
    "supported_formats": [64, 81],
    "min_format": 64,
    "max_format": 81,
    "description": "My NoN pack"
  },
  "animationframework": {
    "id": "creator:my_pack",
    "default_model_revision": 1,
    "name": "My Animation Pack",
    "author": "Creator Name",
    "version": "1.0.0",
    "description": "Adds custom NoN animations."
  }
}
```

The exact `pack_format` can change with Minecraft versions. Use the format accepted by the Minecraft version you target.

The optional `animationframework` section is used by NoN's Loaded Animations screen. It groups animations by pack, shows the pack name/author, and allows the server/host to disable a whole pack or individual animations. Use a stable namespaced `id`; changing it will make existing disabled-pack settings point at the old id. Set `default_model_revision` to the integer shown by the current default pack when the animation is authored; see [Default Model Revisions](#default-model-revisions).

<a id="authoring-and-testing-tools"></a>
### Authoring & Testing Tools

<a id="animationdirector-blockbench-plugin"></a>
#### AnimationDirector Blockbench Plugin

The AnimationDirector Blockbench plugin is called `Multi Actor Animator (Beta)` in Blockbench. It is meant for building NoN multi-actor animations in one Blockbench project without bone-name collisions.

To install it, open Blockbench and go to:

```text
File > Plugins
```

In the plugins menu, press `Load plugin from file` and select the `.js` plugin file in the zip downloadable from the modpage.

![Plugin Button](img/bb_add_plugin.png)

You also need the Blockbench plugin `GeckoLib Models & Animations` to create GeckoLib project types. Install it from Blockbench's plugin marketplace before creating NoN models or animations.

Create NoN animations in a Blockbench GeckoLib animated model Entity project. The exporter expects the GeckoLib/Bedrock model and animation format produced by that project type.

Use the default NoN pack's GeckoLib models as reference when setting up actors, bone names, and texture layout.

The plugin adds two tool menus:

```text
Tools > AnimationDirector Anims
Tools > AnimationDirector Model
```

Main animation workflow:

1. Use `AnimationDirector Anims > Import Actor` to import a GeckoLib/Bedrock `.json` or `.geo.json` model into the current project.
2. Each imported actor receives an internal `actorN_` prefix on its bones, for example `actor1_head`, `actor2_head`. This lets multiple actors share normal bone names like `head` and `body` without Blockbench renaming them unpredictably.
3. Use `Actor Preferences` to set each actor's export name. For NoN, this should usually match the animdef actor key, for example `player`, `wolf`, `wolf1`, or `actor2`.
4. Animate all actors together in one Blockbench animation.
5. Use `AnimationDirector Anims > Export Animations`, select the animation IDs you want, and export one conjoined JSON per animation.

`AnimationDirector Anims > Create Animation Definition Stub` detects the selected animation's actors, stages, loop modes, and cycle lengths. It also accepts optional comma-separated animator credits and remembers them in the Blockbench project for later stub exports. Its optional behavior selector can generate the `system_tags` and metadata fields for a Tease or Stuck animation. Stuck stubs require at least one exact matching content tag and one stage with `escapable: true`.

Exported animation files strip the internal `actorN_` prefix again. If the Blockbench animations are named `wolfmplayer.p1`, `wolfmplayer.p2`, and so on, the exported file is:

```text
wolfmplayer.animation.json
```

Place it directly in the NoN animation folder:

```text
assets/<namespace>/geckolib/animations/afw/wolfmplayer.animation.json
```

The filename supplies the animation ID. Clips inside the file use only the stage and actor key, such as `p1_player`, `p1_wolf`, `p2_player`, and `p2_wolf`.

Actor preferences also support preview textures per actor. These are for easier editing in Blockbench and are saved into the project. Folder-level texture overrides can be enabled there too; those apply recursively and take priority over actor preview textures.

`AnimationDirector Model > Export GeckoLib Model` exports the current model as GeckoLib/Bedrock geometry and can include NoN-specific `afw_bone_textures` metadata when different bones/folders use different textures. This is useful for NoN models that need per-bone texture overrides.

For OptiFine/CEM models, use the separate Blockbench `CEM Template Loader` plugin. Load the vanilla entity template you want to add, copy your custom geometry onto that template, then save/export the `.jem` into `assets/minecraft/optifine/cem/`. Use the default NoN pack's CEM files as reference for naming and gender model variants. Warning: Cube-level rotations are not supported by Optifine models. If you need a rotated shape, place the cube or cube group inside its own folder/bone and rotate that folder/bone instead.

The plugin can re-import a conjoined file using `AnimationDirector Anims > Import Animation`. The animation ID is read from the filename, and the plugin lets you map each stored actor key back to an actor in the current project. It then rebuilds all stages with their `actorN_` prefixes restored. The old split-file import and export actions remain under the `Deprecated` submenu.

<a id="debug-staff"></a>
#### Debug Staff

The Nature Debug Staff is not shown in the creative inventory. Give it to yourself with:

```mcfunction
/give @s needsofnature:debug_staff
```

Right-click an entity that uses NoN energy to print a debug report in chat. The report includes the entity id, UUID, gender, energy, energy gain values, active/pending animation state, aura information, cooldowns, liquid state, and pregnancy state when those systems apply.

Sneak-right-click an energy-capable entity to add `25` energy, up to that entity's max energy. This is useful for testing whether a mob reaches attack/join thresholds and whether pack animations are eligible.

<a id="models-and-rendering"></a>
### Models & Rendering

<a id="local-and-imported-geckolib-models"></a>
#### Local and Imported GeckoLib Models

Models normally use the shared NoN model paths under:

```text
assets/animationframework/geckolib/models/entity/
```

Normal Minecraft resource-pack priority applies to those paths, so the highest-priority version of a model is used by every animation pack. This is useful for animation-only packs that intentionally reuse the default models.

Every pack with a valid explicit `animationframework.id` can also provide local models. The pack ID determines the local model root. For the ID `creator:my_pack`, place models under:

```text
assets/creator/geckolib/models/my_pack/entity/wolf.m.geo.json
assets/creator/geckolib/models/my_pack/entity/wolf.f.geo.json
assets/creator/geckolib/models/my_pack/entity/wolf.mf.geo.json
assets/creator/geckolib/models/my_pack/entity/player.m.geo.json
assets/creator/geckolib/models/my_pack/entity/player.f.geo.json
assets/creator/geckolib/models/my_pack/entity/player.mf.geo.json
```

The `.m` model is used for male entities, `.f` for female entities, and `.mf` for entities with both genders. Local lookup happens for each concrete model variant. A pack that supplies only `wolf.m.geo.json` uses that local model for male wolves while female wolves continue to an imported or shared `wolf.f.geo.json`.

For each requested model, NoN searches the animation's own local root first, matching imports in declared order second, applicable archived default revisions third, and current shared resources last. It searches the correct gender and entity variant across those sources before trying an unsuffixed generic model or an opposite-gender last resort. Missing local models therefore fall back safely instead of producing a missing model.

Only animations whose animdefs come from a pack automatically use that pack's local root. Local models do not replace shared models or models belonging to packs with other IDs. Pack IDs must be unique; duplicate IDs produce a setup warning because they resolve to the same local path.

Use `model_imports` when an animation should use selected local entity models from another pack:

```json
{
  "animationframework": {
    "id": "creator:my_pack",
    "name": "My Animation Pack",
    "author": "Creator Name",
    "model_imports": [
      {
        "pack": "other_creator:model_library",
        "entities": [
          "minecraft:player",
          "minecraft:wolf"
        ]
      },
      {
        "pack": "another_creator:horse_models",
        "entities": [
          "minecraft:horse"
        ]
      }
    ]
  }
}
```

Each import references the other pack's `animationframework.id`. Only entity families listed under `entities` are imported. Import array order is the priority order when more than one imported pack supplies the same model. Imports are non-transitive: importing pack B does not automatically import packs used by B.

An imported pack may contain only models and a valid `pack.mcmeta`; it does not need animation definitions. Disabling that pack's animations in the Loaded Animations screen does not disable its model resources. If the imported resource pack is missing entirely, NoN reports a setup warning and continues to the next import or shared model.

<a id="default-model-revisions"></a>
#### Default Model Revisions

`default_model_revision` protects animation packs from rare, incompatible changes to shared default models. It is a small independent integer, not the default pack's release version:

```json
{
  "animationframework": {
    "id": "creator:my_pack",
    "default_model_revision": 1
  }
}
```

When creating a pack, copy the current revision from the default pack's `pack.mcmeta`. Keep that value afterward. Ordinary default-pack releases, model fixes that preserve bones and locators, and texture-only updates do not require creators to change it. If the field is missing, NoN deliberately treats the pack as legacy revision `1`.

If a shared model later receives an incompatible overhaul, NoN can retain only that model's previous form in a compatibility folder. A pack targeting revision `1` on a revision `3` default pack searches these providers in order:

```text
own local model
explicit model_imports
assets/needsofnature/geckolib/models/default_pack_compat/revision_1/entity/
assets/needsofnature/geckolib/models/default_pack_compat/revision_2/entity/
current shared model
```

The compatibility folders are sparse. Each one contains only models that changed at that revision boundary. This lets an unchanged model continue to the current shared version while an affected model stops at the first historical copy appropriate for the animation pack. Model diagnostics show the target revision, current revision, and the concrete compatibility provider when one is used. A pack that requests a revision newer than the installed default pack produces a setup warning and temporarily falls back to current shared models.

When maintaining the default pack and changing a model incompatibly:

1. If the current revision is `N`, copy the old versions of only the affected model files into `assets/needsofnature/geckolib/models/default_pack_compat/revision_N/entity/`.
2. Archive any custom textures required by those old models at stable, revision-specific resource paths and update the archived model metadata to reference them.
3. Increment the default pack's `default_model_revision` to `N + 1`.
4. Replace the current shared model with the new version.

Increment the revision only when an animation-facing model contract changes, such as renamed, removed, reparented, or substantially repositioned bones or locators. Do not increment it for every default-pack release.

Place a `pack.png` next to `pack.mcmeta` to show a pack image in the Loaded Animations screen:

```text
MyPack/
  pack.mcmeta
  pack.png
  assets/
  data/
```

<a id="geckolib-model-render-metadata"></a>
#### GeckoLib Model Render Metadata

NoN supports a few optional metadata keys at the root of GeckoLib `.geo.json` model files. These keys are not part of vanilla GeckoLib, but NoN reads them from models under:

```text
assets/<namespace>/geckolib/models/
```

<a id="full-model-texture"></a>
##### Full Model Texture

Use `afw_texture` when the entire GeckoLib model should use a custom texture instead of the entity's normal texture. The value is a complete Minecraft texture id, including `textures/` and `.png`:

```json
{
  "afw_texture": "mymod:textures/entity/example/custom.png",
  "format_version": "1.12.0",
  "minecraft:geometry": [
    {
      "description": {
        "identifier": "geometry.example",
        "texture_width": 64,
        "texture_height": 64
      },
      "bones": []
    }
  ]
}
```

The example texture belongs at `assets/mymod/textures/entity/example/custom.png`. If `afw_texture` is omitted, NoN continues to use the entity's normal texture. Because the field belongs to the model file, male, female, combined-gender, size, local, and imported model variants can each select a different texture.

**Warning:** By default, NoN uses the texture selected by Minecraft's normal entity renderer, preserving texture variations such as entity variants, coats, and other state-dependent appearances. `afw_texture` replaces that selection with one fixed texture for every entity using the model. NoN does not automatically apply or distinguish the entity's normal texture variations after a custom texture is specified.

`afw_bone_textures` still takes priority for its listed bones, allowing a model-wide custom texture and additional per-bone textures to be combined.

<a id="bone-texture-overrides"></a>
##### Bone Texture Overrides

Use `afw_bone_textures` when only specific bones should use a different texture. This is useful for extra detail parts, gender-specific feature parts, or appendages that should not share the base entity texture.
The key is an object mapping GeckoLib bone names to texture ids:

```json
{
  "afw_bone_textures": {
    "left_feature": "mymod:textures/entity/example/features.png",
    "right_feature": "mymod:textures/entity/example/features.png"
  },
  "format_version": "1.12.0",
  "minecraft:geometry": [
    {
      "description": {
        "identifier": "geometry.example",
        "texture_width": 64,
        "texture_height": 64
      },
      "bones": [
        {
          "name": "left_feature",
          "pivot": [0, 0, 0]
        },
        {
          "name": "right_feature",
          "pivot": [0, 0, 0]
        }
      ]
    }
  ]
}
```

Only the listed bones are rendered again with the override texture. The bone names must match the final exported GeckoLib bone names exactly.

<a id="emissive-textures"></a>
##### Emissive Textures

NoN automatically discovers emissive textures using the same common `_e` filename convention as ETF. Put the emissive texture beside its normal texture and add `_e` before `.png`:

```text
assets/mymod/textures/entity/example/example.png
assets/mymod/textures/entity/example/example_e.png
```

No model metadata is required for this standard case. The `_e` texture is rendered at full light and should be transparent everywhere that should not glow. It must have the same dimensions as the normal texture; NoN ignores mismatched companions and reports a pack setup warning.

Automatic lookup applies to:

- the model's resolved base texture;
- each additional normal layer texture supplied by an addon;
- every texture assigned through `afw_bone_textures`.

For example, a custom-textured bone only needs the normal mapping below. If `features_e.png` exists beside `features.png`, that bone becomes emissive automatically:

```json
{
  "afw_bone_textures": {
    "glowing_feature": "mymod:textures/entity/example/features.png"
  }
}
```

Each base, layer, and bone texture can have its own `_e` companion, so one model can use multiple emissive textures without listing them manually. NoN performs the lookup itself; ETF does not need to be installed. A lower-priority pack's `_e` texture is not applied to a base texture replaced by a higher-priority pack.

Use `afw_emissive_textures` only for a standalone full-model overlay that is not named as a companion, for example vanilla spider eyes.

The key is an array of texture ids:

```json
{
  "afw_emissive_textures": [
    "minecraft:textures/entity/spider_eyes.png"
  ],
  "format_version": "1.12.0",
  "minecraft:geometry": [
    {
      "description": {
        "identifier": "geometry.example",
        "texture_width": 64,
        "texture_height": 32
      },
      "bones": []
    }
  ]
}
```

Each explicitly listed texture is rendered as a full-light overlay on bones that use the model's normal texture. Bones listed in `afw_bone_textures` are intentionally excluded so unrelated pixels in a model-wide emissive texture cannot make a custom-textured feature glow.

Use `afw_emissive_bone_textures` only when a custom-textured bone needs a nonstandard emissive filename or an explicit override:

```json
{
  "afw_bone_textures": {
    "glowing_feature": "mymod:textures/entity/example/features.png"
  },
  "afw_emissive_bone_textures": {
    "glowing_feature": "mymod:textures/entity/example/custom_glow.png"
  }
}
```

Only the named bone is rendered with the corresponding fullbright texture. An explicit bone mapping takes precedence over an automatically discovered `_e` companion.

<a id="render-settings"></a>
##### Render Settings

Use `afw_render` for model render settings. Set `translucent` to `true` when the entire GeckoLib model should use the translucent pipeline:

```json
{
  "afw_render": {
    "translucent": true
  },
  "format_version": "1.12.0",
  "minecraft:geometry": [
    {
      "description": {
        "identifier": "geometry.example",
        "texture_width": 64,
        "texture_height": 32
      },
      "bones": []
    }
  ]
}
```

This applies the translucent render layer to the whole model.

For models containing both opaque and translucent geometry, use `translucent_bones` instead. The listed bones are omitted from the normal model pass and rendered afterward through an ordered translucent overlay pass:

```json
{
  "afw_render": {
    "translucent_bones": ["outer_shell"]
  },
  "format_version": "1.12.0",
  "minecraft:geometry": [
    {
      "description": {
        "identifier": "geometry.example",
        "texture_width": 64,
        "texture_height": 32
      },
      "bones": [
        {
          "name": "inner",
          "pivot": [0, 0, 0]
        },
        {
          "name": "outer_shell",
          "pivot": [0, 0, 0]
        }
      ]
    }
  ]
}
```

Only the listed bone's own cubes use this pass; child bones remain in the normal pass unless they are listed separately. Keep opaque features such as eyes in normal bones and place only transparent shell geometry in translucent bones. Bone texture overrides continue to apply to translucent bones.

<a id="cem-jem-model-assets"></a>
#### CEM/JEM Model Assets

NoN packs can include OptiFine/EMF-style CEM model files under:

```text
assets/minecraft/optifine/cem/<entity>.jem
```

NoN generates the matching CEM `.properties` rules for gender selection when a pack provides `.jem` files without its own explicit `.properties` file.

Gender allocation convention:

| File            | Used for                                               |
| --------------- | ------------------------------------------------------ |
| `<entity>.jem`  | Primary model. Used for male and male+female entities. |
| `<entity>2.jem` | Female model. Used only for female entities.           |

Examples:

```text
assets/minecraft/optifine/cem/wolf.jem   -> male/primary wolf model
assets/minecraft/optifine/cem/wolf2.jem  -> female wolf model
assets/minecraft/optifine/cem/horse.jem  -> male/primary horse model
assets/minecraft/optifine/cem/horse2.jem -> female horse model
```

If you provide your own `<entity>.properties`, NoN does not generate rules for that entity and your properties file controls selection.

If only a primary model is provided, for example `wolf.jem`, it only targets male and male+female entities. Female entities need a matching `wolf2.jem` if they should use a custom model.

<a id="animation-definitions"></a>
### Animation Definitions (AnimDefs)

<a id="animation-definition-files"></a>
#### Animation Definition Files

Animation definitions are loaded from:

```text
data/<namespace>/afw_animdefs/<animation_id>.json
```

The file path becomes the animation id. For example:

```text
data/creator/afw_animdefs/wolfmplayer.json
```

becomes:

```text
creator:wolfmplayer
```

Subfolders are also part of the id path.

<a id="full-animdef-skeleton"></a>
#### Full Animdef Skeleton

```json
{
  "display_name": "Wolf Encounter",
  "animators": ["Animator One", "Animator Two"],
  "actors": [
    {
      "label": "wolf",
      "entity_types": ["minecraft:wolf"],
      "actor_tags": ["optional.required.tag"],
      "actor_tags_any": ["optional.one", "optional.two"],
      "activity": "active",
      "injector": "V",
      "receiver": false
    },
    {
      "label": "player",
      "entity_types": ["minecraft:player"],
      "activity": "passive",
      "receiver": true,
      "held_item": {
        "items": ["minecraft:carrot", "minecraft:golden_carrot"],
        "hand": "either",
        "prop_slot": "right"
      },
      "prop_floor": "minecraft:lantern"
    }
  ],
  "weight": 1.0,
  "attack_eligible": true,
  "system_tags": ["normal_match"],
  "content_tags": ["doggy"],
  "join_after": ["creator:wolfmplayer"],
  "match_content_on_join": false,
  "required_union_tags": ["some.tag"],
  "speed": 1.0,
  "position_anchor_actor": "player",
  "camera": {
    "torso_tracking": "adaptive",
    "block_requirement_collision": "normal"
  },
  "liquid_gain_multiplier": 1.0,
  "escapable": true,
  "stage_seconds": 10,
  "manual_peak": {
    "priority": 10
  },
  "block_requirements": {
    "type": "center_support",
    "support": "slab",
    "clearance": {
      "width": 1,
      "height": 2,
      "depth": 1
    },
    "surface_radius": 1
  },
  "water": "none",
  "stages": [
    {
      "stage": 1,
      "loop": true,
      "cycle_seconds": 1.0,
      "allow_join": true,
      "speed": 1.0,
      "escapable": true,
      "stage_seconds": 8,
      "stage_duration_multiplier": 1.0,
      "props": {
        "player": {
          "prop_right": "minecraft:carrot",
          "prop_floor": "minecraft:lantern"
        }
      }
    },
    {
      "stage": 2,
      "use_stage": 1,
      "loop": true,
      "cycle_seconds": 1.0,
      "cycle_midpoint_offset_seconds": -0.2,
      "allow_join": false,
      "non_peak": true,
      "escapable": false,
      "stage_seconds": 6,
      "stage_duration_multiplier": 0.5
    }
  ]
}
```

Not every key is needed. The required top-level keys are `actors` and `stages`.

<a id="top-level-animdef-keys"></a>
#### Top-Level Animdef Keys

| Key                      | Type            | Default        | Meaning                                                                                                                                                     |
| ------------------------ | --------------- | -------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `display_name`           | string          | animation id   | Optional player-facing name used by NoN's animation selection and request screens.                                                                          |
| `animators`              | string array    | empty          | Optional individual animation credits shown subtly in Loaded Animations and animation-selection screens.                                                    |
| `actors`                 | array           | required       | Actor constraints. Must contain at least one actor.                                                                                                         |
| `weight`                 | number          | `1.0`          | Random selection weight within the highest-specificity candidate pool. Must be positive.                                                                    |
| `attack_eligible`        | boolean         | `true`         | Set to `false` to exclude this definition from automatic NoN attack selection.                                                                              |
| `system_tags`            | string array    | empty          | Functional tags used by matching, manual peak, defeated poses, and special starts. Tags are normalized lowercase.                                           |
| `content_tags`           | string array    | empty          | General content labels shown in NoN's loaded-animation and request screens, and used to match follow-up defeated animations. Tags are normalized lowercase. |
| `join_after`             | id array        | empty          | Restricts this definition to normal joins that replace one of the listed predecessor animations.                                                            |
| `match_content_on_join`  | boolean         | `false`        | Requires every normal join onto this animation to produce a successor sharing at least one content tag.                                                     |
| `required_union_tags`    | string array    | empty          | All tags must exist in the combined command tags of all selected actors.                                                                                    |
| `speed`                  | positive number | `1.0`          | Base animation speed. Stage speed overrides it.                                                                                                             |
| `block_requirements`     | object          | none           | Requires a wall or center support before the animation can start.                                                                                           |
| `water`                  | string          | `none`         | Water placement requirement: `none`, `surface`, or `underwater`.                                                                                            |
| `position_anchor_actor`  | string          | none           | Actor key used as preferred anchor source. Usually a `label`.                                                                                               |
| `camera`                 | object          | defaults       | Optional third-person camera behavior. It can continuously track the animated torso or ignore the exact blocks selected for a block requirement.           |
| `stages`                 | array           | required       | Multi-stage animation settings.                                                                                                                             |
| `escapable`              | boolean         | `true`         | NoN attack escape UI/logic default. Can be overridden per stage.                                                                                            |
| `stage_seconds`          | integer         | config default | NoN duration override for a stage. `-1` means indefinite.                                                                                                   |
| `liquid_gain_multiplier` | number          | `1.0`          | NoN multiplier for liquid gained from this animation. Negative values are ignored.                                                                          |
| `manual_peak`            | object          | none           | NoN held-item manual peak selection rule.                                                                                                                   |
| `tease`                  | object          | none           | Settings for an animation tagged `tease`.                                                                                                                   |
| `stuck`                  | object          | none           | Escape and damage settings for an animation tagged `stuck`.                                                                                                 |

Do not combine `block_requirements` and `water` for the same animation. The current placement systems are separate.

<a id="camera-tracking"></a>
#### Camera Tracking

The third-person animation camera normally takes a torso target when a stage changes and only corrects it later if the rendered torso has moved far enough. This stable default avoids following every small movement in an animation.

Use continuous torso tracking when the action moves substantially within a stage and should remain centered:

```json
{
  "camera": {
    "torso_tracking": "continuous"
  }
}
```

`continuous` follows the local player's animated `body` bone with very light easing. Each participating player follows their own torso in a multi-player animation. First-person behavior, scroll-wheel zoom, camera collision handling, stage transitions, joins, and queued animations continue to use their normal behavior.

Valid values are `adaptive` and `continuous`. Omitting `camera` or `torso_tracking` uses `adaptive`.

For animations where the selected block structure intentionally passes through the action or the torso, the third-person camera can ignore those requirement blocks:

```json
{
  "camera": {
    "block_requirement_collision": "ignore"
  }
}
```

`ignore` affects only the exact blocks that satisfied this animation instance's `block_requirements`. For a wall this is the required width and minimum height, for a surface footprint it is every support block in the footprint, and for another center support it is the selected support block. Other nearby blocks still stop the camera normally. The blocks remain visible and physical to players and entities; only NoN's third-person animation camera collision ignores them. First-person collision remains unchanged. Omitting the field or using `normal` keeps the default behavior.

<a id="actor-keys"></a>
#### Actor Keys

NoN treats actor constraints as an unordered multiset. JSON actor order does not force which entity becomes which actor slot.

Use `label` when you care about deterministic actor clip keys. For example, this actor:

```json
{
  "label": "wolf1",
  "entity_types": ["minecraft:wolf"]
}
```

uses actor key `wolf1`, so stage 1 uses this key inside `wolfm2player.animation.json`:

```text
p1_wolf1
```

If no label is provided, NoN derives an actor key from the actor constraints. Duplicate keys get `_2`, `_3`, etc.

<a id="actor-constraint-keys"></a>
#### Actor Constraint Keys

| Key              | Type           | Default  | Meaning                                                                                                                         |
| ---------------- | -------------- | -------- | ------------------------------------------------------------------------------------------------------------------------------- |
| `label`          | string         | derived  | Explicit actor key for resource lookup.                                                                                         |
| `entity_types`   | string array   | wildcard | Valid entity ids. Use `minecraft:player` for all player models. Do not use `minecraft:player_slim`.                             |
| `entity_variant` | string         | none     | Exact entity-state variant. Currently supports slime and magma cube sizes as `size_0`, `size_1`, etc.                           |
| `actor_tags`     | string array   | empty    | AND command-tag requirement. The entity must have every tag.                                                                    |
| `actor_tags_any` | string array   | empty    | OR command-tag requirement. The entity must have at least one listed tag.                                                       |
| `activity`       | string         | `active` | `active` or `passive`. NoN attack starts cannot use a passive attacker role.                                                    |
| `prop_left`      | item id string | none     | Default item prop for this actor's left prop bone. Primarily intended for player actors.                                        |
| `prop_right`     | item id string | none     | Default item prop for this actor's right prop bone. Primarily intended for player actors.                                       |
| `prop_floor`     | item id string | none     | Default item prop for this actor's floor prop bone. Primarily intended for player actors.                                       |
| `held_item`      | object         | none     | Requires this living actor to hold a matching item and can copy the complete held stack to a prop bone.                         |
| `injector`       | boolean/string | none     | NoN injector role. Boolean `true` means generic `V`; string values are `V`, `A`, or `M`.                                        |
| `receiver`       | boolean        | inferred | NoN receiver role. If no receivers are explicit, non-injectors become receivers; if still none, player actors become receivers. |

For slime and magma cube actors, `entity_variant` uses the vanilla NBT `Size` value. The smallest slime is `size_0`. A size-specific model should use the same suffix, for example:

```json
{
  "entity_types": ["minecraft:slime"],
  "entity_variant": "size_0"
}
```

```text
assets/animationframework/geckolib/models/entity/slime_size_0.geo.json
assets/animationframework/geckolib/models/entity/magma_cube_size_3.geo.json
```

The included `model_debug_pack` has separate debug animdefs for the normal vanilla slime and magma cube sizes: `size_0`, `size_1`, and `size_3`.

<a id="injector-roles"></a>
#### Injector Roles

NoN supports three injector roles:

| Value | Meaning                                                                                  |
| ----- | ---------------------------------------------------------------------------------------- |
| `V`   | V-slot injection. Adds to V-type liquid tanks.                                           |
| `A`   | A-slot injection. Adds to A-type liquid tanks.                                           |
| `M`   | M-type peak. Does not add liquid; currently triggers the delayed hunger reward behavior. |

Boolean `injector: true` is treated as `V`. Boolean `false` means no injector role.

Do not mark the same actor as both an injector and an explicit receiver. The current parser does not reject this, but it places that actor into both role sets, which is ambiguous and should be treated as unsupported pack syntax. Use separate actor roles instead.

Player tank rules:

| Player gender setting | Liquid tank type |
| --------------------- | ---------------- |
| Female                | `V`              |
| Female + male         | `V`              |
| Male only             | `A`              |

Male-only V-to-A conversion is a NoN debug/compat setting. When enabled, suitable V injections can be treated as A for male-only players if the A slot is not already occupied by another injector.

<a id="actor-activity"></a>
#### Actor Activity

`activity` is used by NoN matching logic.

| Value     | Behavior                                                                         |
| --------- | -------------------------------------------------------------------------------- |
| `active`  | The role can be used by a mob initiating an attack.                              |
| `passive` | The role is valid for non-attack/voluntary joins, but not as the attacking role. |

If omitted, actors are `active`.

<a id="system-tags"></a>
#### System Tags

`system_tags` are functional matching tags.

The legacy key `animation_tags` remains supported so existing packs continue to load. New and updated packs should use `system_tags`; do not provide both keys in the same animdef.

Common tags:

| Tag            | Meaning                                                                                                                                                                             |
| -------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `normal_match` | Allows a tagged animation to still be considered by generic matching. Without this, tagged definitions are ignored by generic NoN matching unless that tag is explicitly requested. |
| `manualpeak`   | Marks an animation as a NoN manual peak candidate.                                                                                                                                  |
| `tease`        | Marks a player animation as a manually selected tease that strengthens its nearby-animation energy influence.                                                                      |
| `stuck`        | Marks an escapable attack-state pose that can replace a normal defeated animation after an exact content-tag match.                                                                |
| `defeated`     | Marks an animation as a defeated pose. Use `content_tags` to describe which source animations fit that pose.                                                                        |

<a id="content-tags"></a>
#### Content Tags

`content_tags` describe the content or position type. For ordinary animation matching they are descriptive rather than actor requirements.

They also act as general player-facing labels for an animation. NoN displays them in the Loaded Animations list and on animation cards in the animation request screen, so keep them short and useful to players.

They also provide data-driven defeated-animation transitions. A defeated animation lists the source content tags that fit its pose:

```json
{
  "system_tags": ["defeated"],
  "content_tags": ["doggy", "from_behind", "on_belly"]
}
```

After an animation ends in defeat, NoN prefers defeated animations sharing at least one content tag with that source animation. Multiple entries use OR semantics: the example accepts `doggy`, `from_behind`, or `on_belly` sources. Tag matching is exact after lowercase normalization.

A side-facing defeated pose can similarly use `system_tags: ["defeated"]` with `content_tags: ["spooning", "side"]`.

If source and defeated animations both omit `content_tags`, they match each other. If no defeated animation shares the source tags, NoN falls back to a weighted random choice among every otherwise valid defeated animation. The normal animdef `weight` controls choices whenever multiple candidates remain.

<a id="attack-eligibility"></a>
#### Attack Eligibility

Use the optional top-level `attack_eligible` boolean when an animation can be used voluntarily but does not make sense as an attack:

```json
{
  "attack_eligible": false,
  "actors": [
    { "label": "mob", "entity_types": ["minecraft:wolf"], "activity": "active" },
    { "label": "player", "entity_types": ["minecraft:player"], "activity": "passive" }
  ],
  "stages": [
    { "stage": 1 }
  ]
}
```

The default is `true`. Setting it to `false` excludes the definition from automatic mob-player attacks, mob-mob attacks, player attacks, post-escape attack gathers, and joins that continue an attack animation.

Voluntary starts, player requests, voluntary joins, direct API starts, and debug starts remain available.

<a id="matching-and-weights"></a>
#### Matching and Weights

NoN first finds all definitions that match the selected actor set and required system tags.

Then it sorts by specificity. Specificity increases with:

| Source                            | Effect              |
| --------------------------------- | ------------------- |
| More specific `entity_types`      | Higher specificity. |
| More required `actor_tags`        | Higher specificity. |
| More `actor_tags_any` constraints | Higher specificity. |
| More `required_union_tags`        | Higher specificity. |

Only the highest-specificity group is used. `weight` is applied inside that group. A low-specificity animation with very high weight will not beat a higher-specificity animation.

<a id="join-chains"></a>
#### Join Chains

Use the optional top-level `join_after` array when an expanded animation should only follow specific active animations. The destination owns the relationship, so a newly added follow-up does not require editing the predecessor.

For example, the two-actor follow-up declares the one-actor animation as its predecessor:

```json
{
  "join_after": ["creator:silverfishmplayer"],
  "actors": [
    { "label": "player", "entity_types": ["minecraft:player"] },
    { "label": "silverfish1", "entity_types": ["minecraft:silverfish"] },
    { "label": "silverfish2", "entity_types": ["minecraft:silverfish"] }
  ],
  "stages": [
    { "stage": 1 }
  ]
}
```

The next expansion can continue the chain independently:

```json
{
  "join_after": ["creator:silverfishm2player"],
  "actors": [
    { "label": "player", "entity_types": ["minecraft:player"] },
    { "label": "silverfish1", "entity_types": ["minecraft:silverfish"] },
    { "label": "silverfish2", "entity_types": ["minecraft:silverfish"] },
    { "label": "silverfish3", "entity_types": ["minecraft:silverfish"] }
  ],
  "stages": [
    { "stage": 1 }
  ]
}
```

Every entry must be an exact namespaced animation id. One destination may list multiple accepted predecessors. NoN reports malformed entries, self references, and missing predecessor ids as setup warnings.

The rule applies to normal mob joins, player joins, and joins onto defeated animations. It does not restrict direct starts, queued animations, or NoN debug joins.

Chains are strict while at least one declared follower of the current animation is enabled. Only matching declared followers are considered; if they fail actor, block, protector, or other eligibility checks, the join fails instead of selecting an unrelated animation. Weights still choose among the eligible followers using the normal specificity rules.

Disabled followers do not activate a chain. If every declared follower is disabled in the loaded-animation settings, ordinary unrestricted join matching resumes. A definition with `join_after` still cannot follow an unlisted predecessor.

Use the source-owned `match_content_on_join` option when any expanded successor is acceptable as long as it shares the current animation's content type:

```json
{
  "content_tags": ["doggy", "from_behind"],
  "match_content_on_join": true
}
```

At least one exact content tag must appear on both definitions. The example accepts successors tagged `doggy` or `from_behind`. Multiple tags use OR semantics, and comparison happens after lowercase normalization. Untagged and disjoint successors are rejected.

This restriction is strict and has no fallback. If no eligible successor shares a tag, the join fails and the current animation continues. Enabling the option without any `content_tags` produces a setup warning and rejects every normal join; use stage-level `allow_join: false` instead when an animation should not accept joins at all.

`match_content_on_join` has the same scope as `join_after`: normal mob joins, player joins, and joins onto defeated animations. It does not affect fresh starts, queued animations, defeated-animation selection, or NoN debug joins. When both options apply, the successor must satisfy both the exact `join_after` chain and the shared-content requirement. Animation weights are applied after these filters.

<a id="stages"></a>
#### Stages

Stages are required. Each stage has its own loop, cycle, join, and NoN timing settings.

| Key                             | Type            | Default           | Meaning                                                                                                                                                                                                                                            |
| ------------------------------- | --------------- | ----------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `stage`                         | number          | required          | Stage number, for example `1`.                                                                                                                                                                                                                     |
| `loop`                          | boolean         | `true`            | Whether this stage loops.                                                                                                                                                                                                                          |
| `cycle_seconds`                 | number          | `0`               | GeckoLib cycle length, converted to ticks with `ceil(seconds * 20)`.                                                                                                                                                                               |
| `cycle_midpoint_offset_seconds` | number          | `0.0`             | Loop-only timing warp. Negative moves the cycle midpoint/impact earlier; positive moves it later. Visual playback, sound cues, reactive impacts, and cue-derived NoN effects use the shifted timing. Ignored with a warning on non-looping stages. |
| `allow_join`                    | boolean         | `true`            | Whether NoN joins can expand this stage.                                                                                                                                                                                                           |
| `speed`                         | positive number | top-level `speed` | Stage-specific animation speed.                                                                                                                                                                                                                    |
| `use_stage`                     | number          | self              | Play another stage's GeckoLib animation file while retaining this stage's metadata.                                                                                                                                                                |
| `props`                         | object/null     | inherited         | Stage item prop overrides. `null` clears all props.                                                                                                                                                                                                |
| `non_peak`                      | boolean         | false             | NoN peak marker. The first marked stage is treated as the peak.                                                                                                                                                                                    |
| `escapable`                     | boolean         | top-level/default | Whether the attack escape prompt/bar is active on this stage.                                                                                                                                                                                      |
| `stage_seconds`                 | integer         | top-level/config  | NoN duration override. `-1` is indefinite; normal positive values are clamped to `1..300`.                                                                                                                                                         |
| `stage_duration_multiplier`     | number          | `1.0`             | NoN stage duration multiplier for looping stages. It scales the resolved duration after `stage_seconds`, top-level `stage_seconds`, or config defaults, then cycle alignment is applied. Valid range is `0.05..20.0`.                              |

Example:

```json
{
  "stages": [
    {
      "stage": 1,
      "loop": true,
      "cycle_seconds": 1.2,
      "allow_join": true,
      "stage_seconds": 8,
      "stage_duration_multiplier": 1.0
    },
    {
      "stage": 2,
      "loop": true,
      "cycle_seconds": 0.8,
      "allow_join": false,
      "non_peak": true,
      "escapable": false,
      "stage_seconds": 6,
      "stage_duration_multiplier": 0.5
    }
  ]
}
```

`stage_duration_multiplier` changes how long a looping stage lasts without changing GeckoLib playback speed. Use `speed` when the clip itself should play faster or slower.

Example: if the normal stage setting is `15` seconds and three non-peak stages each use `"stage_duration_multiplier": 0.3333`, each of those stages lasts about `5` seconds before cycle alignment. A peak stage still uses the peak stage setting unless it has its own `stage_seconds` or multiplier.

<a id="stage-props"></a>
#### Stage Props

Actor-level `prop_left`, `prop_right`, and `prop_floor` define static defaults. Stage-level `props` can override or clear them.

Props are primarily meant for player actors and player prop bones. They are technically parsed for any actor, but entity models usually do not expose compatible prop bones unless the model was authored for that.

The three fields render on the `propleft`, `propright`, and `propfloor` GeckoLib bones respectively. Floor props use Minecraft's ground item-display transform; animate or rotate the `propfloor` bone when a different authored orientation is needed.

```json
{
  "actors": [
    {
      "label": "player",
      "entity_types": ["minecraft:player"],
      "prop_right": "minecraft:stick",
      "prop_floor": "minecraft:lantern"
    }
  ],
  "stages": [
    {
      "stage": 1,
      "props": {
        "player": {
          "prop_right": "minecraft:carrot",
          "prop_floor": null
        }
      }
    },
    {
      "stage": 2,
      "props": {
        "player": null
      }
    },
    {
      "stage": 3,
      "props": null
    }
  ]
}
```

`"player": null` clears that actor's props. `"props": null` clears all props for that stage. Explicitly clearing a slot also hides a dynamic held-item prop assigned to that slot.

<a id="held-item-requirements-and-dynamic-props"></a>
#### Held-Item Requirements and Dynamic Props

Add `held_item` to any living actor to require a held item. `items` accepts one item/tag string or an array of alternatives. Entries beginning with `#` are Minecraft item tags, not creative inventory groups.

```json
{
  "label": "armed_zombie",
  "entity_types": ["minecraft:zombie"],
  "held_item": {
    "items": "minecraft:diamond_sword",
    "hand": "main",
    "prop_slot": "right"
  }
}
```

Use an item tag to support every matching vanilla or modded item:

```json
"held_item": {
  "items": ["#minecraft:swords", "examplemod:special_blade"],
  "hand": "either",
  "prop_slot": "right"
}
```

| Key         | Type                | Default | Meaning                                                                                  |
| ----------- | ------------------- | ------- | ---------------------------------------------------------------------------------------- |
| `items`     | string/string array | required | Alternative exact item ids and/or `#item_tag` entries. Any one match is accepted.       |
| `hand`      | string              | `main`  | Source hand: `main`, `off`, or `either`. `either` checks the main hand first.            |
| `prop_slot` | string              | none    | Optional destination: `left`, `right`, or `floor`. Omit it for a requirement-only rule. |

When `prop_slot` is present, NoN snapshots the complete matching stack when the animation starts. Enchantments, components, custom names, custom model data, and other stack-specific appearance are retained. The real held item is not consumed or moved.

Held-item requirements are checked again immediately before the animation starts. If an actor changes or removes the required item while approaching or waiting, the start is cancelled. Explicit debug-forced starts may ignore only this held-item check; if no matching stack exists, no dynamic prop is shown.

The destination uses the existing `propleft`, `propright`, or `propfloor` GeckoLib bone. The actor's model must contain that bone for the item to be visible.

Dynamic held items override static actor and stage props in the same slot. A stage can temporarily hide the dynamic item with an explicit `null`:

```json
{
  "stage": 2,
  "props": {
    "armed_zombie": {
      "prop_right": null
    }
  }
}
```

An actor-level `null` hides all three props for that actor during the stage, while top-level `"props": null` hides every prop for that stage. On later stages where the slot is not explicitly cleared, the held item appears again.

Effective priority is: gameplay runtime override, matched held-item stack, stage static prop, then actor static default. Matching item-specific animations retain their normal animation `weight`; they receive no hidden selection priority.

<a id="manual-peak-held-item-rules"></a>
#### Manual Peak Held-Item Rules

Manual peak candidates are normal animdefs with `system_tags` containing `manualpeak`.

Use the normal actor-level `held_item` rule for held-item-specific variants. `manual_peak` only needs the manual-selection priority:

```json
{
  "system_tags": ["manualpeak"],
  "actors": [
    {
      "label": "player",
      "entity_types": ["minecraft:player"],
      "held_item": {
        "items": ["minecraft:carrot", "minecraft:golden_carrot"],
        "hand": "either",
        "prop_slot": "right"
      }
    }
  ],
  "manual_peak": {
    "priority": 10
  }
}
```

| Key        | Type    | Default | Meaning                                               |
| ---------- | ------- | ------- | ----------------------------------------------------- |
| `priority` | integer | `0`     | Higher-priority matching manual peak rules are selected first. |

If no held-item rule matches, NoN falls back to a normal manual peak animation.

The older `manual_peak.held_item`, `held_items`, `prop_from_held_item`, and `held_item_prop_slot` fields remain accepted for existing packs. New and updated packs should use the actor-level rule so the same syntax works for manual peak and every other animation type.

<a id="tease-animations"></a>
#### Tease Animations

Tease animations are one-player animations shown in their own section at the top of the held manual-animation menu. They are never selected by a short press of the manual peak key.

```json
{
  "actors": [
    {
      "label": "player",
      "entity_types": ["minecraft:player"]
    }
  ],
  "system_tags": ["tease"],
  "tease": {
    "near_animation_energy_multiplier": 2.0
  },
  "stages": [
    {
      "stage": 1,
      "loop": true,
      "cycle_seconds": 2.0,
      "stage_seconds": -1,
      "allow_join": true
    }
  ]
}
```

| Key                                    | Type   | Default | Range      | Meaning                                                                                          |
| -------------------------------------- | ------ | ------- | ---------- | ------------------------------------------------------------------------------------------------ |
| `near_animation_energy_multiplier`     | number | `1.0`   | `1.0-20.0` | Multiplies the existing nearby-animation energy gain influence while this tease is playing.      |

The tease multiplier is applied after the normal nearby-animation and accessory multipliers. If several animations influence the same mob, NoN keeps the strongest nearby-animation multiplier rather than adding every source together.

Only the player performing the tease acts as its influence source. A nearby mob must have an enabled actor- and gender-compatible animation with that player and must have line of sight. This compatibility check intentionally ignores held-item, placement, and block requirements; those requirements are evaluated normally if the mob later attempts to join or start an animation.

<a id="stuck-animations"></a>
#### Stuck Animations

Stuck animations are one-player attack-state poses that may be selected in place of an ordinary defeated animation. A stuck definition is eligible only when it shares at least one exact `content_tags` value with the attack animation that just peaked. Multiple matching stuck definitions are selected using their normal `weight` values. If no stuck definition matches, NoN uses the ordinary defeated-animation selector unchanged.

```json
{
  "actors": [
    {
      "label": "player",
      "entity_types": ["minecraft:player"]
    }
  ],
  "system_tags": ["stuck"],
  "content_tags": ["stuckfence"],
  "stuck": {
    "initial_escape_difficulty_multiplier": 2.0,
    "final_escape_difficulty_multiplier": 0.75,
    "ease_delay_seconds": 10,
    "ease_duration_seconds": 30,
    "joined_escape_difficulty_multiplier": 1.5,
    "damage_behavior": "stop_on_damage"
  },
  "stages": [
    {
      "stage": 1,
      "loop": true,
      "cycle_seconds": 3.0,
      "stage_seconds": -1,
      "allow_join": true,
      "escapable": true
    }
  ]
}
```

| Key                                      | Type   | Default          | Range       | Meaning                                                                                                     |
| ---------------------------------------- | ------ | ---------------- | ----------- | ----------------------------------------------------------------------------------------------------------- |
| `initial_escape_difficulty_multiplier`   | number | `1.0`            | `0.1-10.0`  | Escape difficulty before easing starts. Above `1` is harder; below `1` is easier.                           |
| `final_escape_difficulty_multiplier`     | number | `1.0`            | `0.1-10.0`  | Difficulty after easing finishes. It cannot be harder than the initial value.                               |
| `ease_delay_seconds`                     | integer | `0`              | `0-600`     | Time before the stuck escape begins becoming easier.                                                        |
| `ease_duration_seconds`                  | integer | `15`             | `0-600`     | Time spent interpolating from initial to final difficulty. `0` changes immediately after the delay.         |
| `joined_escape_difficulty_multiplier`    | number | `1.0`            | `0.1-10.0`  | Escape difficulty applied after an actor joins the stuck pose and to later actor joins in that same chain.  |
| `damage_behavior`                        | string | `stop_on_damage` | see below   | Controls what incoming damage does while the player is still in the stuck pose.                             |

Valid damage behavior values are:

| Value            | Behavior                                                        |
| ---------------- | --------------------------------------------------------------- |
| `stop_on_damage` | Damage applies normally and stops the stuck animation.          |
| `ignore_damage`  | Damage applies normally without stopping the stuck animation.   |
| `block_damage`   | Damage is prevented while the stuck animation remains active.   |

As easing progresses, health, player energy, and random escape penalties are gradually neutralized. Escape-progress decay also falls to exactly zero at the end of the easing period, so an escapable stuck animation cannot remain impossible forever. Per-stage `escapable` values are still respected.

When another actor joins, the configured joined multiplier replaces the stuck easing curve. It remains active through further actor-join replacements without multiplying repeatedly, and clears when that join chain ends. Server-controlled player-versus-player mash contests keep their own balancing and do not use this multiplier.

Stuck animations also appear as attack animations in the held manual-animation menu, which is useful for testing placement and escape settings.

<a id="defeated-animations"></a>
#### Defeated Animations

Defeated animations are one-actor player animdefs using the `defeated` system tag. Their pose compatibility is described through `content_tags`:

```json
{
  "actors": [
    {
      "label": "player",
      "entity_types": ["minecraft:player"]
    }
  ],
  "system_tags": ["defeated"],
  "content_tags": ["doggy", "from_behind", "on_belly"],
  "stages": [
    {
      "stage": 1,
      "loop": true,
      "cycle_seconds": 1.0,
      "allow_join": true,
      "stage_seconds": -1,
      "escapable": true
    }
  ]
}
```

For a back-facing defeated animation, keep the same `defeated` system tag and provide suitable content tags. For example, the default back pose accepts `content_tags` of `missionary` and `on_back`.

NoN compares the source and defeated definitions using the content-tag rules described under [Content Tags](#content-tags). This relationship is owned by the defeated animation, so adding a new defeated pose does not require editing every source animation.

<a id="animation-assets-and-timeline-events"></a>
### Animation Assets & Timeline Events

<a id="geckolib-animation-assets"></a>
#### GeckoLib Animation Assets

NoN's primary animation format stores every stage and actor clip for one animation in a single conjoined file:

```text
assets/<namespace>/geckolib/animations/afw/<animation_id>.animation.json
```

For animation ID `mymod:wolfmplayer`, the file is:

```text
assets/mymod/geckolib/animations/afw/wolfmplayer.animation.json
```

The filename is the only place that stores `wolfmplayer`. Keys inside the `animations` object use the strict `p<stage>_<actorKey>` format:

```json
{
  "format_version": "1.8.0",
  "animations": {
    "p1_wolf": {
      "loop": true,
      "animation_length": 1.0,
      "bones": {}
    },
    "p1_player": {
      "loop": true,
      "animation_length": 1.0,
      "bones": {}
    },
    "p2_wolf": {
      "animation_length": 2.0,
      "bones": {}
    },
    "p2_player": {
      "animation_length": 2.0,
      "bones": {}
    }
  }
}
```

Do not repeat the animation ID in these keys. For example, `wolfmplayer.p1_wolf` is invalid in a conjoined file. The actor key should match the corresponding actor key in the animdef whenever possible. NoN may also resolve an actor clip by its entity type path when no direct actor-key clip exists.

<a id="animation-previews"></a>
#### Animation Previews

Any animation may provide a square preview image. NoN currently displays it in the Loaded Animations list and, where relevant, in player animation selection, role, and consent screens. Other menus may reuse the same preview in the future.

The file is technically optional. Released player x player animations should have one because players select and approve them directly.

<a id="creating-animation-preview"></a>
##### Creating an Animation Preview in Blockbench

1. Import the provided [blue male-role texture](img/animation_previews/player_male.png) and [pink female-role texture](img/animation_previews/player_female.png) into the Blockbench project. Assign the blue texture to male-role actors and the pink texture to female-role actors in the `Tools > AnimationDirector Anims > Actor Preferences` screen. The provided colors are `#59B8E8` and `#E889B5`.

![Male-role actors in blue and female-role actors in pink](img/animation_previews/actor_role_colors.png)

<br>

2. Turn on `Shading` in the 'Preview Options'

![Shading button in Blockbench](img/animation_previews/shading_button.png)

<br>

3. Go to the animation tab. Select a good frame of the animation. position the Blockbench camera at an angle that clearly communicates the pose and both actors' roles. Keep every important body part inside a roughly square composition and avoid angles where one actor hides the other.

4. Use Blockbench's `Screenshot` button to capture the preview.

![Blockbench Screenshot button](img/animation_previews/blockbench_screenshot_button.png)

<br>

5. Name the PNG exactly after the animation ID path and place it in the pack's preview folder. For animation ID `<namespace>:<animation_id>`, use:

```text
assets/<namespace>/textures/afw/previews/<animation_id>.png
```

For example, `creator:playermplayer-cowgirl` uses:

```text
assets/creator/textures/afw/previews/playermplayer-cowgirl.png
```

NoN displays a generic placeholder when the image is missing.

Set the optional top-level `display_name` animdef field to give the animation a player-facing name in the Loaded Animations list and in selection, role, and consent screens. If it is omitted, NoN displays the animation id instead. The preview filename and animation identity always continue to use the real animation id.

Use the optional top-level `animators` array to credit one or more people for an individual animation:

```json
"animators": ["Animator One", "Animator Two"]
```

NoN displays this as a subtle `by Animator One, Animator Two` line in Loaded Animations and animation-selection screens. If the field is omitted or empty, no byline is shown. These per-animation credits are separate from the pack-wide author in `pack.mcmeta`.

<a id="reactive-impact-cues"></a>
#### Reactive Impact Cues

`reactiveimpact` is the most important sound/effect cue for normal NoN animations. It should be placed on the actual repeated impact/contact moment of a GeckoLib animation cycle.

Even though it lives inside GeckoLib `sound_effects`, it does more than play audio. NoN uses `reactiveimpact` timing for:

- impact sound selection and playback
- player FOV impact pulse
- Buttplug.io reactive impact pulses
- peak-stage impact behavior when the cue happens during the configured `non_peak` stage
- timing liquid gain pulses across the peak stage instead of granting the full amount instantly
- timing affected by `cycle_midpoint_offset_seconds`, so shifted cycles also shift cue-derived NoN behavior

NoN merges all actor sound keyframes for an animation stage into one anchor track. Equivalent cues at the same second are deduplicated, while different cues at the same second are preserved. Because of that, put each important semantic cue on the actor file where it is easiest to maintain and avoid unnecessary duplicates across actors.

Example:

```json
{
  "animations": {
    "p1_player": {
      "animation_length": 1.0,
      "sound_effects": {
        "0.25": {
          "effect": "reactiveimpact"
        }
      }
    }
  }
}
```

Use `effect: "reactiveimpact"` for NoN impact behavior. NoN then picks an appropriate impact sound automatically based on the current context.

Do not replace `reactiveimpact` with a direct sound id unless you intentionally want audio only. A cue like `"effect": "needsofnature:impactwet01"` plays sound, but it will not trigger reactive impact mechanics.

Use `reactiveimpact_silent` when the cue should trigger all normal reactive-impact mechanics without playing NoN's automatic impact sound. Add an optional `sound` field to the same keyframe when custom audio should play alongside it:

```json
{
  "sound_effects": {
    "0.25": {
      "effect": "reactiveimpact_silent",
      "sound": "yourpack:custom_impact"
    }
  }
}
```

The `sound` field also accepts an inline `random(...)` pool. The silent variant still controls FOV pulses, Buttplug.io reactions, peak liquid distribution and every other reactive-impact mechanic.

For looping stages, cue times are offsets inside one animation cycle. If the stage loops several times, the cue repeats every cycle. For non-looping stages, the cue fires once when that timestamp is reached.

<a id="other-special-sound-cues"></a>
#### Other Special Sound Cues

NoN also recognizes these specialized cue effects:

| Cue effect             | Used for                                                                                                   |
| ---------------------- | ---------------------------------------------------------------------------------------------------------- |
| `birth`                | Birth animations. Spawns one offspring/egg at the cue and controls multi-birth stage routing.              |
| `fillbottle_prop_fill` | Fill-bottle animation. Swaps the visual bottle prop to the filled bottle and plays the vanilla fill sound. |

Birth animations are one-actor player animdefs with `system_tags` containing `birth`. Entity-specific birth animations can use `birth_<entity_id>` where the entity id is lowercased and non-alphanumeric characters are replaced by `_`, for example `birth_minecraft_wolf`.

<a id="normal-non-sound-cues"></a>
#### Normal NoN Sound Cues

These cues are normal audio cues. They only play sound and do not trigger any special behavior.

Use them as the cue `effect` value, for example:

```json
{
  "effect": "impactdry01"
}
```

You can also use the full sound id form, for example `needsofnature:impactdry01`.

| Cue                             | Meaning                                                                  |
| ------------------------------- | ------------------------------------------------------------------------ |
| `impactdry`                     | Random dry impact selector. Picks one of `impactdry01` to `impactdry11`. |
| `impactwet`                     | Random wet impact selector. Picks one of `impactwet01` to `impactwet16`. |
| `impactdry01` ... `impactdry11` | Specific dry impact sounds.                                              |
| `impactwet01` ... `impactwet16` | Specific wet impact sounds.                                              |
| `wet01` ... `wet12`             | Wet/slosh-style sounds.                                                  |
| `shot_in01` ... `shot_in03`     | Shot-in sounds.                                                          |
| `shot_out01` ... `shot_out06`   | Shot-out sounds.                                                         |
| `motion01` ... `motion10`       | Motion sounds.                                                           |
| `retract01` ... `retract03`     | Retract sounds.                                                          |

<a id="inline-random-sound-pools"></a>
#### Inline Random Sound Pools

For one-off sound variation, put a comma-separated `random(...)` pool in the sound keyframe's `effect` field. NoN chooses one entry with equal probability each time the cue fires.

```json
{
  "sound_effects": {
    "0.25": {
      "effect": "random(needsofnature:wet02, needsofnature:wet04, needsofnature:impactwet05)",
      "volume": 1.0,
      "pitch": 1.0
    }
  }
}
```

Every entry must be a fully qualified sound event id containing its namespace. The syntax can be entered directly into Blockbench's normal sound-effect field and survives normal GeckoLib and AnimationDirector exports. Inline pools are audio-only and do not trigger special mechanics such as `reactiveimpact`, `birth`, or `fillbottle_prop_fill`.

Random selection happens independently on each client. Use a normal Minecraft `sounds.json` event instead when a pool should be reusable, weighted, or provide subtitles.

<a id="particle-keyframes"></a>
#### Particle Keyframes

NoN supports GeckoLib `particle_effects` keyframes in actor animation files.

Example:

```json
{
  "animations": {
    "playerf_manual_peak.p1": {
      "animation_length": 1.0,
      "particle_effects": {
        "0.25": {
          "effect": "needsofnature:liquid_particle_falling",
          "locator": "v_locator",
          "pre_effect_script": "color_source=actor"
        }
      }
    }
  }
}
```

| Key                 | Type        | Meaning                                                                                                                                                |
| ------------------- | ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `effect`            | particle id | Registered particle type to spawn. NoN supports simple types directly and data-bearing types through integration hooks such as NoN's liquid particles. |
| `locator`           | string      | Optional GeckoLib model locator name. If found, the particle spawns at that posed locator.                                                             |
| `pre_effect_script` | string      | Optional particle options interpreted by NoN integrations.                                                                                             |

For NoN liquid particles, `pre_effect_script` accepts `color_source=tank` or `color_source=actor`. Omitting it is the same as `tank` and keeps using the player's current liquid-tank color. `actor` uses the configured liquid color of the actor that owns the particle cue; a player actor therefore uses the `minecraft:player` liquid color.

Locators are defined in the GeckoLib model on the bone that should carry the particle position:

```json
{
  "name": "body",
  "pivot": [0, 24, 0],
  "locators": {
    "v_locator": [0, 12, -1]
  }
}
```

If `v_locator` is missing or cannot be resolved, NoN tries `a_locator` for that particle. If that fallback is also unavailable, or if any other requested locator is unavailable, the particle is not spawned.

<a id="placement-requirements"></a>
### Placement Requirements

<a id="wall-block-requirements"></a>
#### Wall Block Requirements

Wall requirements look for a wall of a given height and optional width range, plus a directional clearance area in front of it.

```json
{
  "block_requirements": {
    "type": "wall",
    "height": {
      "min": 2,
      "max": 3
    },
    "width": 2,
    "clearance": {
      "width": 1,
      "height": 2,
      "depth": 2
    }
  }
}
```

`height` can also be an exact integer:

```json
{
  "block_requirements": {
    "type": "wall",
    "height": 2
  }
}
```

An integer wall `width` is a minimum. `"width": 2` requires at least two contiguous wall columns and accepts wider walls. Use an object when an upper bound is also required:

```json
{
  "block_requirements": {
    "type": "wall",
    "height": 2,
    "width": {
      "min": 2,
      "max": 2
    }
  }
}
```

Every required wall column must independently match the wall blocks, configured height and valid floor rules. The animation anchor is centered across the matched columns, including between the blocks for an even width.

A configured `max` checks contiguous matching blocks at every required height level. By default it checks only the wall's width axis: left and right relative to the selected wall face. NoN may select the end face of a connected structure so blocks in front of or behind that face remain outside the width check. A neighboring wall block on the checked axis counts even when it does not form a complete column of the required height.

The width object has two optional flags:

- `"both_axes": true` also applies `max` in front of and behind the selected wall block. This prevents the end of a longer wall from being treated as a narrower structure viewed from another face.
- `"include_obstructions": true` counts any block with a collision shape toward `max`, even when it does not match `blocks`. Non-colliding decorations such as grass and torches are ignored.

Both flags default to `false` and only affect a configured `max`. To require one freestanding vertical fence column, use:

```json
{
  "block_requirements": {
    "type": "wall",
    "height": {
      "min": 3
    },
    "width": {
      "max": 1,
      "both_axes": true,
      "include_obstructions": true
    },
    "blocks": "#minecraft:fences"
  }
}
```

The vertical stack is controlled by `height`. In this example, any collidable block touching any horizontal side of the three required fence levels makes the structure ineligible. Only the required `height.min` levels are checked; additional height above that remains unrestricted.

`clearance.width` starts at `1`. `width: 1` checks only the space directly in front of the complete matched wall span, `width: 2` adds one free block on both sides, and each higher value adds one more block per side. Clearance `height` and `depth` are also clamped to at least `1`.

Wall requirements with a `blocks` predicate can additionally require a specific face of each block to point toward the animation area:

```json
{
  "block_requirements": {
    "type": "wall",
    "height": {
      "min": 1
    },
    "blocks": "minecraft:furnace",
    "facing": "front",
    "clearance": {
      "width": 1,
      "height": 2,
      "depth": 2
    }
  }
}
```

`facing` is relative to the animation instead of the world, so the same requirement works with blocks facing north, south, east, or west.

| Value   | Required block face toward the animation area |
| ------- | --------------------------------------------- |
| `front` | Front                                         |
| `back`  | Back                                          |
| `left`  | Left, from the block's forward perspective    |
| `right` | Right, from the block's forward perspective   |

Useful examples for `facing` include:

- Furnaces, where `front` is the furnace opening.
- Chests, where `front` is the side with the latch.
- Stairs, where the orientation can control which side of the stair holds the animation. For stairs, `back` is the stepped face.

This field is only supported for wall requirements and requires `blocks`. Every block required by the configured wall width and height must match the selected relative face. A candidate block without a direction-valued `facing` block-state property does not match. Vertical `up` and `down` facing values are not supported for walls.

<a id="low-wall-requirement-example"></a>
##### Example: Low Wall Requirement

Example animdef block for an animation that needs a low wall:

```json
{
  "block_requirements": {
    "type": "wall",
    "height": {
      "min": 1
    },
    "clearance": {
      "width": 1,
      "height": 2,
      "depth": 2
    }
  }
}
```

This means NoN searches for a full block wall at least one block high. A valid start position must also have a free directional clearance area in front of that wall. With `width: 1`, the clearance checks only the space directly in front of the matched wall area. With `depth: 2` and `height: 2`, that clear area must be two blocks deep and two blocks tall.

This shows a valid block alignment. Red glass indicates the spaces that must stay free for the directional clearance check. Orange glass indicates the animation anchor position.

![Valid low wall block alignment](img/block_requirements/wall_block.png)

<br>

This shows the same wall requirement using a fence instead of a full block. For wall requirements fences, walls and glass panes are supported and will move the animations slightly for better alignment. Red glass indicates the spaces that must stay free for the directional clearance check. Orange glass indicates the animation anchor position.

![Valid low wall fence alignment](img/block_requirements/wall_fence.png)

<br>

This Blockbench reference shows how to position the actors relative to the animation origin and the wall surface used by this requirement.

![Blockbench alignment for a low wall requirement](img/block_requirements/blockbench_wall_low.png)

<br>

<a id="two-wide-fence-requirement-example"></a>
##### Example: Two-Wide Fence Requirement

```json
{
  "block_requirements": {
    "type": "wall",
    "height": {
      "min": 1
    },
    "width": 2,
    "blocks": "#minecraft:fences",
    "clearance": {
      "width": 1,
      "height": 2,
      "depth": 2
    }
  }
}
```

Every required wall position must match the `minecraft:fences` block tag. The two fence columns must be horizontally adjacent, have valid ground below them, and provide the configured clearance directly in front of the complete two-block animation area. Because `width` is an integer minimum, a wider contiguous fence wall is also valid. Since the width requirement is even it will place the Anchor in between two blocks.

This shows the minimum valid alignment with two adjacent fence columns. Red glass indicates the spaces that must stay free for the directional clearance check. Orange glass indicates the animation anchor centered between the two fence columns.

![Valid minimum two-wide fence wall alignment](img/block_requirements/wall_fence_min_width_2.png)

<br>

This Blockbench reference shows the animation origin centered across the two-block-wide fence area and how the actors should align with its surface.

![Blockbench alignment for a two-wide fence requirement](img/block_requirements/blockbench_wall_fence_min_width_2.png)

<br>

<a id="center-support-block-requirements"></a>
#### Center Support Block Requirements

Center support requirements anchor an animation inside a selected support block. This is useful for animations that should happen on top of, inside of, or against a specific surface such as a block, slab, bed, or custom block.

There are two placement modes:

| Placement               | Meaning                                                                            |
| ----------------------- | ---------------------------------------------------------------------------------- |
| `directional_clearance` | Default. Requires free space next to the support block, similar to wall clearance. |
| `surface_footprint`     | Requires a same-height support surface with a literal width/depth footprint.       |

Supported `support` values:

| Support       | Meaning                                                                                                      |
| ------------- | ------------------------------------------------------------------------------------------------------------ |
| `full_block`  | Full-height block support. Top slabs and double slabs count as full blocks.                                  |
| `slab`        | Bottom slabs only. Top slabs and double slabs do not count.                                                  |
| `half_height` | Bottom slabs and beds. Beds and bottom slabs are treated as compatible for same-height footprint checks.     |
| `surface`     | Custom support based on a block id or block tag in `blocks`. Required when targeting beds only, chests, etc. |

Use `support: "surface"` with `blocks` when the animation should only run on specific blocks:

```json
{
  "block_requirements": {
    "type": "center_support",
    "support": "surface",
    "blocks": "#minecraft:beds",
    "placement": "surface_footprint",
    "surface_footprint": {
      "width": 2,
      "depth": 2,
      "height": 2
    }
  }
}
```

`blocks` accepts namespaced block ids or block tags:

```json
"blocks": "minecraft:chest"
```

```json
"blocks": ["minecraft:chest", "minecraft:trapped_chest"]
```

```json
"blocks": "#minecraft:beds"
```

<a id="directional-clearance-center-support"></a>
##### Directional Clearance Center Support

This is the default center-support mode. It finds a support block and then checks for free directional space next to it.

```json
{
  "block_requirements": {
    "type": "center_support",
    "support": "slab",
    "placement": "directional_clearance",
    "clearance": {
      "width": 1,
      "height": 2,
      "depth": 2
    },
    "surface_radius": 1
  }
}
```

`placement` can be omitted here because `directional_clearance` is the default.

| Key              | Type    | Meaning                                                                                                |
| ---------------- | ------- | ------------------------------------------------------------------------------------------------------ |
| `clearance`      | object  | Directional free-space check next to the support, similar to wall clearance.                           |
| `surface_radius` | integer | Checks surrounding blocks around the support for compatible surface height and vertical air clearance. |

This means NoN searches for a bottom slab and anchors the animation inside that slab instead of above it. The directional clearance still needs a free space next to the slab; with `width: 1`, it only checks the selected forward line, while `width: 2` adds one checked block on both sides. With `depth: 2`, each checked line must be two blocks deep. `surface_radius: 1` checks the 3x3 area around the support so nearby blocks are compatible with the slab surface height and the required air above it is clear.

This shows a valid slab surface where the surrounding blocks are also bottom slabs. Red glass indicates the spaces that must stay free for the directional clearance and vertical air checks. Orange glass indicates the animation anchor position.

![Valid bottom-slab alignment with surrounding slabs](img/block_requirements/slab_surrounded.png)

<br>
This shows a valid slab support where the surrounding blocks are air. Red glass indicates the spaces that must stay free for the directional clearance and vertical air checks. Orange glass indicates the animation anchor position.

![Valid bottom-slab alignment with surrounding air](img/block_requirements/slab_air.png)

<br>

This Blockbench reference shows how to position the actors relative to the animation origin inside the selected support block and its directional clearance.

![Blockbench alignment for directional center support](img/block_requirements/blockbench_center_support_directional.png)

<br>

<a id="surface-footprint-center-support"></a>
##### Surface Footprint Center Support

Surface footprint mode does not use directional clearance. Instead, it requires a same-height support surface under the animation.

```json
{
  "block_requirements": {
    "type": "center_support",
    "support": "half_height",
    "placement": "surface_footprint",
    "surface_footprint": {
      "width": 2,
      "depth": 2,
      "height": 2,
      "margin": 1
    }
  }
}
```

| Key                        | Type    | Default | Meaning                                                    |
| -------------------------- | ------- | ------- | ---------------------------------------------------------- |
| `surface_footprint.width`  | integer | `1`     | Literal footprint width in blocks.                         |
| `surface_footprint.depth`  | integer | `1`     | Literal footprint depth in blocks.                         |
| `surface_footprint.height` | integer | `2`     | Required empty vertical space above every footprint block. |
| `surface_footprint.margin` | integer | `0`     | Extra free-space border around the footprint.              |

The selected support block is one corner of the footprint. NoN tries all four forward directions and both side directions, so even footprints such as `2x2` can be found from any corner.

Footprint cells must be valid support blocks of the same support category and same normalized support height. Margin cells do not need support blocks, but they must be empty and fluid-free from the support surface height upward.

###### Example: 2x2 Half-Height Footprint

This example accepts a 2x2 area made from bottom slabs, beds, or a compatible mixture of both.

```json
{
  "block_requirements": {
    "type": "center_support",
    "support": "half_height",
    "placement": "surface_footprint",
    "surface_footprint": {
      "width": 2,
      "depth": 2,
      "height": 2
    }
  }
}
```

This shows a valid 2x2 half-height footprint. Bottom slabs and beds may be used. Orange glass indicates the animation anchor position.

![Valid 2x2 half-height footprint](img/block_requirements/surface_footprint_half_height_2x2.png)

<br>

This shows a valid mixed 2x2 half-height footprint using two bottom slabs and one bed. This shows that `half_height` allows bottom slabs and beds to share the same required surface. Orange glass indicates the animation anchor position.

![Valid mixed 2x2 half-height footprint](img/block_requirements/surface_footprint_half_height_2x2_mixed.png)

<br>

###### Example: Bed-Only Footprint

```json
{
  "block_requirements": {
    "type": "center_support",
    "support": "surface",
    "blocks": "#minecraft:beds",
    "placement": "surface_footprint",
    "surface_footprint": {
      "width": 2,
      "depth": 2,
      "height": 2,
      "margin": 0
    }
  }
}
```

This shows a valid bed-only 2x2 footprint. Orange glass indicates the animation anchor position.

![Valid bed-only 2x2 footprint](img/block_requirements/surface_footprint_beds_2x2.png)

<br>

This Blockbench reference shows how to align the actors with the animation origin and the orientation of the required 2x2 bed surface.

![Blockbench alignment for a bed-only 2x2 footprint](img/block_requirements/blockbench_surface_footprint_beds_2x2.png)

<br>

###### Example: 2x2 Half-Height Footprint With Margin

This example requires the same 2x2 surface, but also needs one block of free space around the footprint.

```json
{
  "block_requirements": {
    "type": "center_support",
    "support": "half_height",
    "placement": "surface_footprint",
    "surface_footprint": {
      "width": 2,
      "depth": 2,
      "height": 2,
      "margin": 1
    }
  }
}
```

This shows a valid 2x2 half-height footprint with a one-block margin. Red glass indicates the spaces that must stay free around the whole footprint. Orange glass indicates the animation anchor position.

![Valid 2x2 half-height footprint with margin](img/block_requirements/surface_footprint_half_height_2x2_margin.png)

<br>

<a id="water-requirements"></a>
#### Water Requirements

```json
{
  "water": "surface"
}
```

Valid values:

| Value        | Meaning                                    |
| ------------ | ------------------------------------------ |
| `none`       | No water requirement.                      |
| `surface`    | Requires suitable water surface placement. |
| `underwater` | Requires suitable underwater placement.    |

<a id="entity-profiles"></a>
### Entity Profiles

Entity profiles are server-data JSON files for per-entity gameplay and liquid defaults:

```text
data/<namespace>/non_entity_profiles/<file>.json
```

Example:

```json
{
  "mixed_liquid_color": "#f2ebbf",
  "entries": [
    {
      "entity": "minecraft:spider",
      "pregnancy_chance_percent": 5,
      "offspring_min": 1,
      "offspring_max": 3,
      "birth_entity": "minecraft:spider",
      "birth_mode": "egg",
      "energy_gain_multiplier": 1.5,
      "gather_speed_multiplier": 1.25,
      "multi_actor_join_chance_percent": 100,
      "egg": {
        "start_size": 0.5,
        "end_size": 1.0,
        "texture": "needsofnature:textures/entity/pregnancy_egg/minecraft_spider.png",
        "health": 2.0
      },
      "gender_spawn": {
        "male_chance": 100,
        "female_chance": 0,
        "both_chance": 0
      },
      "liquid": {
        "gain_ml": 45,
        "color": "#decfff"
      }
    },
    {
      "entity": "minecraft:enderman",
      "pregnancy_chance_percent": 8,
      "offspring_min": 2,
      "offspring_max": 6,
      "birth_item": "minecraft:ender_pearl",
      "birth_mode": "item"
    },
    {
      "entity": "minecraft:turtle",
      "offspring_min": 1,
      "offspring_max": 4,
      "birth_block": "minecraft:turtle_egg",
      "birth_mode": "egg_block"
    },
    {
      "entity": "minecraft:player",
      "liquid": {
        "gain_ml": 15
      }
    },
    {
      "entity": "minecraft:bee",
      "liquid": {
        "type": "honey",
        "gain_ml": 10,
        "color": "#d1a83a"
      }
    }
  ]
}
```

| Key                               | Type             | Meaning                                                                   |
| --------------------------------- | ---------------- | ------------------------------------------------------------------------- |
| `mixed_liquid_color`              | `#RRGGBB` string | Optional global mixed-liquid color override.                              |
| `entries`                         | array            | Entity-specific profile entries.                                          |
| `entity`                          | entity id        | Entity type to which the supplied profile fields apply.                   |
| `pregnancy_chance_percent`        | integer          | Optional per-entity pregnancy chance override, clamped to `0..100`.       |
| `offspring_min`                   | integer          | Minimum result count, clamped to `1..16`.                                 |
| `offspring_max`                   | integer          | Maximum result count, raised to at least `offspring_min`.                 |
| `birth_entity`                    | entity id        | Entity spawned by `egg` or `direct` mode. Omit to use `entity`.           |
| `birth_item`                      | item id          | Item produced by `item` mode. Required when `birth_mode` is `item`.       |
| `birth_block`                     | block id         | Block produced after an `egg_block` egg finishes growing.                 |
| `birth_mode`                      | string           | `egg`, `egg_block`, `direct`, or `item`.                                  |
| `energy_gain_multiplier`          | number           | Passive energy buildup multiplier. Valid range: `0.1..10.0`.              |
| `gather_speed_multiplier`         | number           | Gathering movement multiplier. Valid range: `0.1..4.0`.                   |
| `multi_actor_join_chance_percent` | integer          | Optional override for this mob's 3+ actor join roll, clamped to `0..100`. |
| `deterrence_class`                | string           | Optional `passive`, `neutral`, or `hostile` mob-deterrence override.      |
| `egg`                             | object           | Optional egg settings used by `egg` and `egg_block` modes.                |
| `egg.start_size`                  | number           | Egg render and hitbox scale at spawn, clamped to `0.05..4.0`.             |
| `egg.end_size`                    | number           | Egg render and hitbox scale when fully grown, clamped to `0.05..4.0`.     |
| `egg.texture`                     | texture id       | Egg texture. Omit or leave empty to use the default NoN egg texture.      |
| `egg.health`                      | number           | Egg health, clamped to `0.5..100.0`.                                      |
| `gender_spawn`                    | object           | Optional gender spawn chances for this entity.                            |
| `male_chance`                     | integer          | Male-only spawn percentage.                                               |
| `female_chance`                   | integer          | Female-only spawn percentage.                                             |
| `both_chance`                     | integer          | Both-gender spawn percentage.                                             |
| `liquid`                          | object           | Optional liquid gain, type, and color defaults for this entity.           |
| `liquid.type`                     | string           | `entity` (default) or `honey`.                                            |
| `liquid.gain_ml`                  | integer          | Liquid gain amount, clamped to `0..1000`.                                 |
| `liquid.color`                    | `#RRGGBB` string | Entity liquid color.                                                      |

For `item` mode, the rolled `offspring_min` to `offspring_max` value is the number of items produced. Each `birth` cue drops one item. Items cannot be picked up by players until the complete birth animation stops; if the animation stops before every expected cue, all remaining items are dropped immediately and then unlocked.

For `egg_block` mode, each pregnancy result first spawns the same growing GeckoLib egg used by `egg` mode. When fully grown, that temporary entity becomes the default state of `birth_block`. NoN first adds it to a nearby matching block with an integer `eggs` property when that stack is not full, allowing vanilla turtle eggs to combine up to four per block. Otherwise it safely places the block at the egg or within two horizontal blocks. Solid blocks are never overwritten. If no valid position exists, the result is dropped as a block item instead. Once placed, the block follows all of its normal vanilla or modded behavior.

<a id="custom-pregnancy-egg-models"></a>
#### Custom Pregnancy Egg Models

Profiles using `"birth_mode": "egg"` or `"birth_mode": "egg_block"` can provide a custom GeckoLib model for each entity that caused the pregnancy. Place the model at:

```text
assets/needsofnature/geckolib/models/entity/pregnancy_egg/<entity_namespace>_<entity_path>.geo.json
```

The filename is derived from the source `entity`, not from `birth_entity`. The namespace separator and any slashes in the entity path become underscores:

| Source `entity`                | Model filename                          |
| ------------------------------ | --------------------------------------- |
| `minecraft:spider`             | `minecraft_spider.geo.json`             |
| `minecraft:cave_spider`        | `minecraft_cave_spider.geo.json`        |
| `examplemod:creatures/big_bug` | `examplemod_creatures_big_bug.geo.json` |

No model field is needed in the entity profile. If the entity-specific model is missing, NoN falls back to:

```text
assets/needsofnature/geckolib/models/entity/pregnancy_egg/default.geo.json
```

Set `egg.texture` in the source entity profile when the custom model needs a matching texture:

```json
"egg": {
  "start_size": 0.5,
  "end_size": 1.0,
  "texture": "examplemod:textures/entity/pregnancy_egg/big_bug.png",
  "health": 2.0
}
```

The custom model changes the egg's appearance only. Its render scale and collision size still use `egg.start_size` and `egg.end_size`. The default NoN pack's `minecraft_spider.geo.json` can be used as a reference.

If `pregnancy_chance_percent` is omitted, NoN uses the default pregnancy chance from the normal NoN settings. If it is present, it overrides that default for this entity.

With liquid-based pregnancy enabled, `direct` and `item` modes use the stored-liquid conception system. The egg-producing `egg` and `egg_block` modes remain peak-based instead: one eligible egg-producing donor is selected per peak and rolls the fixed `pregnancy_chance_percent`. Their liquid is still added to the tank normally, but it cannot cause a later liquid-based pregnancy.

If `energy_gain_multiplier` is omitted, it defaults to `1.0`. It multiplies passive energy buildup, including nearby-animation and player-aura acceleration. It does not modify energy granted directly by commands, items, or gameplay events.

If `gather_speed_multiplier` is omitted, it defaults to `1.0`. It scales the mob's existing vanilla navigation speed while gathering, joining, or approaching an active animation. It preserves the entity's land, swimming, amphibious, or flying movement controller; for example, `0.8` is 20% slower and `1.25` is 25% faster than the normal NoN gathering speed. Amphibious entities use the same multiplier both in and out of water.

If `multi_actor_join_chance_percent` is omitted, NoN uses the global **Join chance (3+ actors)** setting. Set it to `100` to make this entity always pass that probability roll. Matching, block requirements, join permissions, and gathering still apply normally.

If `deterrence_class` is omitted, NoN classifies tamed mobs at runtime, vanilla anger-based mobs as `neutral`, hostile monsters as `hostile`, and remaining mobs as `passive`. Use `deterrence_class` only when that automatic category is unsuitable for a custom or unusual entity. `tamed` is not a valid profile value: a currently tamed mob always uses the tamed scolding behavior regardless of this field.

`male_chance`, `female_chance`, and `both_chance` must total exactly `100`.

Profiles are applied in datapack priority order. Entries for the same entity merge field by field, including individual `egg` and `liquid` fields, so a pack can override only the values it owns without erasing unrelated settings from another pack. Within one pack, files are processed in id order and later supplied fields win.

--- ---

<a id="accessories"></a>
## Accessories

<a id="data-driven-accessories"></a>
### Data-Driven Accessories

Accessory item JSON files are loaded from:

```text
assets/needsofnature/non_accessory_items/<item>.json
```

The namespace must be `needsofnature`. These files create item registry entries at startup, so a game restart is required after adding or removing them.

Accessories require Trinkets to be installed. If Trinkets is missing, NoN skips accessory item registration and continues without the accessory system.

Example:

```json
{
  "id": "iron_plug",
  "max_count": 1,
  "show_in_item_group": true,
  "trinkets_slots": ["legs/v", "legs/a"],
  "skin_overlays": {
    "v": "needsofnature:item/iron_plug_v",
    "a": "needsofnature:item/iron_plug_a"
  },
  "effects": {
    "liquid_decay_multiplier": {
      "value": 0.25,
      "tooltip": "positive"
    },
    "equalize_liquid_decay_context": {
      "value": true,
      "tooltip": "positive"
    },
    "player_energy_gain_multiplier": {
      "value": 3.0,
      "tooltip": "negative"
    }
  },
  "item_texture": "needsofnature:item/iron_plug"
}
```

<a id="accessory-item-keys"></a>
### Accessory Item Keys

| Key                                       | Type         | Default               | Meaning                                                                                        |
| ----------------------------------------- | ------------ | --------------------- | ---------------------------------------------------------------------------------------------- |
| `id`                                      | item id/path | required              | Item id. Namespace defaults to `needsofnature`; other namespaces are rejected.                 |
| `max_count`                               | integer      | `1`                   | Stack size, clamped to `1..64`. Durability forces this to `1`.                                 |
| `max_durability`                          | integer      | `0`                   | Max item durability, clamped to `0..100000`.                                                   |
| `show_in_item_group`                      | boolean      | `true`                | Whether the item appears in the NoN creative tab.                                              |
| `trinkets_slots`                          | string/array | `["legs/v","legs/a"]` | Slots this item can be equipped in.                                                            |
| `occupies_slots`                          | string/array | empty                 | Extra slots blocked while this item is equipped.                                               |
| `skin_overlays`                           | object       | empty                 | Optional player skin overlay textures for V/A/D slots.                                         |
| `effects`                                 | object       | neutral               | Accessory stat modifiers.                                                                      |
| `blocks_injector_types`                   | string/array | empty                 | `V`, `A`, or `M` injector types blocked by this item.                                          |
| `exclusive_group`                         | string       | none                  | Prevents equipping multiple items in the same group.                                           |
| `protection_durability_cost`              | integer      | `1`                   | Durability loss when protection blocks an animation.                                           |
| `ignore_injector_slot_visual_shedding`    | boolean      | `false`               | Keeps overlay visible during matching injector animations. Pregnancy can still force shedding. |
| `ignore_injector_slot_effect_suppression` | boolean      | `false`               | Keeps effects active during matching injector animations.                                      |
| `v_injection_durability_cost`             | integer      | `0`                   | Durability loss on V injection.                                                                |
| `a_injection_durability_cost`             | integer      | `0`                   | Durability loss on A injection.                                                                |
| `blocks_pregnancy`                        | boolean      | `false`               | Blocks pregnancy behavior while active.                                                        |
| `item_texture`                            | asset id     | none                  | Simple generated item model texture.                                                           |
| `item_model`                              | asset id     | generated             | Optional custom item model id.                                                                 |

Valid trinket slot names:

```text
legs/v
legs/a
legs/d
v
a
d
```

Short names normalize to `legs/<slot>`.

Slot availability:

| Slot | Available for                   |
| ---- | ------------------------------- |
| `V`  | Female and female+male players. |
| `A`  | All player genders.             |
| `D`  | Male and female+male players.   |

<a id="accessory-enchantments"></a>
### Accessory Enchantments

Enchantments are enabled automatically; accessory definitions do not need an enchantment field.

| Accessory type                  | Supported enchantments                                        |
| ------------------------------- | ------------------------------------------------------------- |
| Every accessory                 | Curse of Binding and Curse of Vanishing                       |
| `max_durability` greater than 1 | Unbreaking, Mending, Curse of Binding, and Curse of Vanishing |
| `max_durability` equal to 1     | Curse of Binding and Curse of Vanishing                       |

An accessory with `max_durability: 1` is treated as single-use, so Unbreaking and Mending cannot be applied. Accessories with more than one durability point support Unbreaking at an enchanting table or through an enchanted book. Mending and both curses follow their normal vanilla acquisition rules.

Unbreaking affects durability consumed by NoN accessory behavior, such as a protector blocking an injection. Mending can repair a damaged accessory while it is equipped in a Trinkets slot. Curse of Binding prevents normal removal from that slot, while Creative mode retains its vanilla bypass.

<a id="accessory-effects"></a>
### Accessory Effects

Each effect can be written as a raw value:

```json
{
  "effects": {
    "liquid_capacity_add": 100
  }
}
```

or as an object with an explicit tooltip color:

```json
{
  "effects": {
    "liquid_capacity_add": {
      "value": 100,
      "tooltip": "positive"
    }
  }
}
```

Tooltip color values:

| Value      | Color      |
| ---------- | ---------- |
| `positive` | green      |
| `negative` | red        |
| `neutral`  | white      |
| `#RRGGBB`  | custom RGB |

Supported effect keys:

| Effect                                 | Type    | Clamp           | Meaning                                                  |
| -------------------------------------- | ------- | --------------- | -------------------------------------------------------- |
| `liquid_decay_multiplier`              | number  | `0..20`         | Multiplies liquid decay.                                 |
| `equalize_liquid_decay_context`        | boolean | n/a             | Removes sneaking/water decay speed changes while active. |
| `player_energy_gain_multiplier`        | number  | `0..20`         | Multiplies player energy buildup.                        |
| `liquid_capacity_add`                  | integer | `-10000..10000` | Adds liquid tank capacity.                               |
| `liquid_gain_multiplier`               | number  | `0..20`         | Multiplies liquid gained from animations.                |
| `filled_effect_multiplier`             | number  | `0..20`         | Multiplies filled penalty/effect behavior.               |
| `pregnancy_chance_multiplier`          | number  | `0..20`         | Multiplies pregnancy chance.                             |
| `pregnancy_duration_multiplier`        | number  | `0.01..20`      | Multiplies pregnancy duration.                           |
| `mess_gain_multiplier`                 | number  | `0..20`         | Multiplies mess gain.                                    |
| `destroyed_skin_damage_multiplier`     | number  | `0..20`         | Multiplies destroyed-skin damage.                        |
| `attack_escape_hits_add`               | integer | `-49..49`       | Adds required attack escape hits.                        |
| `attack_escape_damage_multiplier`      | number  | `0..20`         | Multiplies escape damage.                                |
| `player_energy_aura_multiplier`        | number  | `0..20`         | Multiplies player energy aura.                           |
| `near_animation_mob_energy_multiplier` | number  | `0..20`         | Multiplies near-animation mob energy gain.               |

Liquid tank and pregnancy effects are only active from the correct active tank slot. Non-tank effects can still apply from other valid slots.

<a id="accessory-skin-overlays"></a>
### Accessory Skin Overlays

`skin_overlays` maps slot letters to texture ids:

```json
{
  "skin_overlays": {
    "v": "needsofnature:item/my_accessory_v",
    "a": "needsofnature:item/my_accessory_a",
    "d": "needsofnature:item/my_accessory_d"
  }
}
```

The value is converted to a texture path. This example resolves to:

```text
assets/needsofnature/textures/item/my_accessory_v.png
```

For an A+V item using one shared overlay, point both entries at the same texture:

```json
{
  "skin_overlays": {
    "v": "needsofnature:item/my_dual_slot_accessory_overlay",
    "a": "needsofnature:item/my_dual_slot_accessory_overlay"
  }
}
```

<a id="animation-blocking-accessory-example"></a>
### Animation-Blocking Accessory Example

```json
{
  "id": "my_dual_slot_accessory",
  "max_count": 1,
  "max_durability": 8,
  "show_in_item_group": true,
  "trinkets_slots": ["legs/v", "legs/a"],
  "occupies_slots": ["legs/v", "legs/a"],
  "skin_overlays": {
    "v": "needsofnature:item/my_dual_slot_accessory_overlay",
    "a": "needsofnature:item/my_dual_slot_accessory_overlay"
  },
  "blocks_injector_types": ["V", "A"],
  "exclusive_group": "slot_blocker",
  "protection_durability_cost": 1,
  "item_texture": "needsofnature:item/my_dual_slot_accessory"
}
```

<a id="recipes-and-advancements"></a>
### Recipes and Advancements

Recipes use normal Minecraft data-pack paths:

```text
data/<namespace>/recipe/<recipe_id>.json
```

Recipe unlock advancements use:

```text
data/<namespace>/advancement/recipes/<category>/<recipe_id>.json
```

Example shaped accessory recipes based on the default pack's slot-blocking accessory patterns:

Single-slot accessory variant with iron ingots and a gold nugget:

```json
{
  "fabric:load_conditions": [
    {
      "condition": "fabric:all_mods_loaded",
      "values": [
        "trinkets"
      ]
    }
  ],
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "IGI",
    "III",
    " I "
  ],
  "key": {
    "I": "minecraft:iron_ingot",
    "G": "minecraft:gold_nugget"
  },
  "result": {
    "id": "needsofnature:my_slot_accessory"
  }
}
```

Single-slot accessory variant with iron ingots and a copper nugget:

```json
{
  "fabric:load_conditions": [
    {
      "condition": "fabric:all_mods_loaded",
      "values": [
        "trinkets"
      ]
    }
  ],
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "ICI",
    "III",
    " I "
  ],
  "key": {
    "I": "minecraft:iron_ingot",
    "C": "minecraft:copper_nugget"
  },
  "result": {
    "id": "needsofnature:my_other_slot_accessory"
  }
}
```

Dual-slot accessory variant with gold ingots and an emerald:

```json
{
  "fabric:load_conditions": [
    {
      "condition": "fabric:all_mods_loaded",
      "values": [
        "trinkets"
      ]
    }
  ],
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "GEG",
    "GGG",
    " G "
  ],
  "key": {
    "G": "minecraft:gold_ingot",
    "E": "minecraft:emerald"
  },
  "result": {
    "id": "needsofnature:my_dual_slot_accessory"
  }
}
```

---

<a id="addon-api"></a>
## Addon API

Addon mods compile against NeedsOfNature directly and use the public API types under `com.nonid.api`. Classes under `com.nonid.internal` are implementation details and may change.

Server-side mutation and animation-control methods must run on the Minecraft server thread. Client event classes under `com.nonid.api.client` must be registered from a client initializer.

<a id="player-state"></a>
### Player State

`NonPlayerApi` exposes immutable snapshots and controlled mutations for NoN player systems:

```java
NonPlayerStateSnapshot state = NonPlayerApi.getState(player);
NonGender gender = NonPlayerApi.getGender(player);
NonLiquidTankSnapshot tank = NonPlayerApi.getLiquidTank(player);
NonMessSnapshot mess = NonPlayerApi.getMess(player);
NonDestroyedSkinSnapshot rippedSkin = NonPlayerApi.getDestroyedSkin(player);
NonPregnancySnapshot pregnancy = NonPlayerApi.getPregnancy(player);
NonAccessoryEffectsSnapshot accessories = NonPlayerApi.getAccessoryEffects(player);
```

Server-side methods include `setGender`, `addLiquid`, `drainLiquid`, `addMess`, `cleanMess`, `clearMess`, `damageDestroyedSkin`, `repairDestroyedSkin`, `beginPregnancy`, and `clearPregnancy`. These methods apply NoN's normal clamping, feature toggles, synchronization, and public events. A mutation returning `false` was rejected or did not change the stored state; for example, `beginPregnancy` returns `false` when pregnancy is disabled, the player is ineligible, or an `ALLOW_PREGNANCY` listener vetoes it.

`NonEntityApi` provides energy access for any supported living entity:

```java
if (NonEntityApi.hasEnergy(entity)) {
    NonEnergySnapshot energy = NonEntityApi.getEnergy(entity);
    NonEntityApi.addEnergy(entity, 10);
}
```

`NonEvents` contains server-side hooks for gender, liquid tank, mess, ripped skin, and pregnancy changes. It also exposes `MODIFY_LIQUID_GAIN` and `ALLOW_PREGNANCY` decision hooks.

<a id="animation-queries-and-control"></a>
### Animation Queries and Control

`NonAnimationApi` is the public server-side entry point for NoN's animation system. It exposes immutable definition, pack, stage, actor, block-requirement, candidate, and active-session views.

Common queries:

```java
List<NonAnimationDefinition> definitions = NonAnimationApi.getLoadedDefinitions();
NonAnimationDefinition definition = NonAnimationApi.getDefinition(animationId);
boolean enabled = NonAnimationApi.isEnabled(animationId);

NonAnimationCandidate match = NonAnimationApi.findMatch(
        world,
        List.of(firstActor, secondActor),
        NonAnimationApi.MatchOptions.startEligible(firstActor)
);
```

`findMatch` and `findCandidates` use the same actor constraints, weights, enabled-state checks, tags, block requirements, water requirements, and placement rules as NoN. `MatchOptions` can require system tags, an active actor, a predecessor animation, exclusions, or an addon-provided candidate filter.

`NonAnimationCandidate.actorUuids()` and `actorKeys()` are parallel lists in the same order as the actor list supplied by the addon. Keep those lists paired when presenting or starting a selected role assignment. Active `NonAnimationSession` snapshots also expose parallel actor UUID and role-key lists, using NoN's deterministic session order.

Starting a matched animation:

```java
UUID instanceId = NonAnimationApi.startMatched(
        world,
        List.of(firstActor, secondActor),
        NonAnimationApi.MatchOptions.startEligible(firstActor),
        NonAnimationApi.StartOptions.defaults()
);
```

For explicit control, use `start`, `canStart`, `getSession`, `getCurrentStage`, `advanceStage`, `advanceToStage`, `multiplySpeed`, `stop`, `stopAll`, `enqueue`, and `clearQueue`. Start options control damage behavior, attacker ignoring, placement anchor, requester, metadata, debug chat, and whether multiple player actors are allowed. `start` validates exact actor-role compatibility and environment requirements before starting. Supplied role keys must be in the same order as the supplied actors. `startMatched` uses the start option's placement anchor when present; otherwise it carries the match option's anchor into the start.

`NonAnimationEvents` provides `BEFORE_START`, `STARTED`, `STAGE_CHANGED`, and `STOPPED`. `BEFORE_START` is a veto hook; returning `false` prevents the instance from starting before actors enter their animation state.

```java
NonAnimationEvents.BEFORE_START.register(context -> {
    return !context.metadata().containsKey("my_addon:block_start");
});

NonAnimationEvents.STARTED.register(context -> {
    UUID instanceId = context.session().instanceId();
});
```

`NonActorEvents.PROVIDE` lets addons contribute temporary actor tags without mutating command tags:

```java
NonActorEvents.PROVIDE.register(entity -> {
    return entity.hasStatusEffect(myEffect) ? Set.of("my_addon:affected") : Set.of();
});
```

<a id="player-interaction-profiles"></a>
### Player Interaction Profiles

`NonPlayerInteractionApi` resolves and updates per-world player request/attack profiles. It honors profiles disabled by the host and synchronizes mutations back to the owning player's profile screen. Its mutation methods return `true` only when the stored profile actually changed.

```java
boolean mayRequest = NonPlayerInteractionApi.canRequest(requester, target);
boolean mayAttack = NonPlayerInteractionApi.canAttack(attacker, target);

NonPlayerInteractionApi.setOverride(
        target,
        requester,
        NonPlayerInteractionApi.Mode.REQUESTS_ONLY
);
```

Available modes are `DENY_ALL`, `REQUESTS_ONLY`, `REQUESTS_AND_ATTACKS`, and `AUTO_ACCEPT`.

<a id="client-animation-hooks"></a>
### Client Animation Hooks

Client-only extension points are:

| Class                        | Hook                                                                                                           |
| ---------------------------- | -------------------------------------------------------------------------------------------------------------- |
| `NonAnimationRenderEvents`   | Override the GeckoLib model, base texture, layers, per-bone textures/items/visibility, or hidden cube indices. |
| `NonAnimationSoundEvents`    | Replace, modify, or suppress resolved animation sound cues.                                                    |
| `NonAnimationParticleEvents` | Replace or suppress animation particle keyframes after locator position resolution.                            |

Register these only from client code. Do not reference client API classes from a dedicated-server initializer.

NoN's own render, sound, and particle behavior is a built-in baseline rather than a public event listener. This means a generated ripped/mess texture cannot prevent addon callbacks from running. Render callback results compose field by field: later non-null model and texture values win, layer lists append without duplicates, and maps merge with later entries winning for the same key. Return an explicitly empty list or map to clear an earlier value. For sound and particle events, the first non-null addon result wins and replaces NoN's fallback; built-in non-audio cue behavior still runs.
