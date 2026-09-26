NON PACK EXAMPLE

This minimal NoN/AFW pack template contains only:
- pack.mcmeta for pack information
- data/example/afw_animdefs/ for animation definitions
- assets/example/geckolib/animations/afw/ for conjoined animations
- assets/example/geckolib/models/example_pack/entity/ for optional local models
- assets/animationframework/geckolib/models/entity/ for optional shared overrides

The local model path is derived from animationframework.id. The included ID is
example:example_pack. If it is changed to creator:my_pack, move the local model
folder to assets/creator/geckolib/models/my_pack/entity/.

Before using the template, replace the example namespace in the animation and
data paths, update animationframework.id and the other pack information, and
move the local model folder to the matching ID-derived path.

Use local models for geometry or bone structures specific to this pack. Local
models are selected independently for each gender and entity variant. Missing
local files continue through model_imports, compatible archived default models,
and then the current shared model set. Keep default_model_revision in pack.mcmeta
set to the revision shown by the default pack used while creating the animations.
Missing fields intentionally use legacy revision 1.

The animationframework model folder is a global shared override. Files placed
there may affect every animation pack that reaches shared fallback. Most packs
should delete that placeholder and reuse NoN's current shared models instead.
