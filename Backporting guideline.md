# Backporting guideline
If you want to contribute to porting Create back to Minecraft 1.7.10, then this guide is for you.

[TOC]

## Source material
Current version of ReCreate is based on [Create 0.4.1](https://github.com/Creators-of-Create/Create/tree/mc1.17/dev).
Why use this exact version? - you may ask.
The answer is pretty simple: Create 0.4.1 is the final version before the **Full Steam** update, which added a way for players to build trains. Due to the way I'm working on this port, porting from versions 0.5 and above would require commenting out a lot of code which would be unusable until later updates.
I also want to implement a physics engine of sorts (probably Rapier, used in Create Aeronautics) with the release of the **Full Steam** update for ReCreate to make trains derail, like they do in [**Create Interactive**](https://www.youtube.com/watch?v=GjVxY_PsYks) and to lay a foundation for future physics-based updates :wink:.

## Roadmap
Obviously heavilly based on the [Create version history](https://wiki.createmod.net/users/changelogs/).
Despite the fact that we are using classes from version 0.4.1, we should add features in the chronological order, with the exceptions being:
- Features removed by 0.4.1. No need to backport these
- Features reworked by 0.4.1. We should backport the latest version of the feature intead
- Core features like goggles, wrench and Pondering mechanic. These should be added ASAP
- Features required by others on 0.4.1. For example in 0.4.1 crushing wheels are crafted using the mechanical crafter, which requires us to backport it earlier

1. ReCreate 1.0 - Initial release. Features added:
	- [x]Shafts
	- [x]Cogwheels
	- [x]Mechanical Belts
	- [ ]Casings
	- [ ]Encased Shafts
	- [ ]Encased Belts
	- [x]Gearboxes
	- [x]Clutch
	- [x]Gearshift
	- [x]Water Wheel
	- [ ]Encased Fan and related Processing recipes
	- [ ]Crushing Wheels with recipes
	- [ ]Mechanical Press with recipes
	- [x]Mechanical Piston
	- [x]Piston Poles
	- [x]Linear Chassis
	- [x]Mechanical Bearing
	- [x]Windmill Bearing
	- [x]Sails
	- [x]Rotation Chassis
	- [ ]Mechanical Drill
	- [ ]Mechanical Harvester
	- [ ]Mechanical Saw
	- [ ]Mechanical Crafter
	- [ ]Turntable
	- [ ]Redstone Contact
	- [ ]Redstone Links
	- [ ]Pulse Repeaters
	- [ ]Pulse Extenders
	- [ ]Threshold Switch
	- [ ]Item Vault
	- [ ]Funnels
	- [ ]Belt Tunnels
	- [ ]Smart Observer
		- Backport vanilla Observer for this one
	- [x]Engineer's Goggles
	- [x]Superglue
	- [x]Wrench
	- [ ]Pondering mechanic
	- [ ]Pulse Extender
	- [ ]Encased Cogwheels and Encased Large Cogwheels
	- [ ]Experience Nuggets
	- [ ][Optional] Schematics and Schematicannon. May be a little bit difficult, since it would require backporting an entire nbt-based structures system.
2. ReCreate 1.1. Corresponding to Create 0.2. Features added:
	- [ ]Mechanical Mixer
	- [ ]Mechanical Plough
	- [ ][Optional] Furnace Engine and Flywheel
	- [ ]The Deployer
	- [ ]Hand Crank
	- [ ]Gauges to measure speed and stress
	- [ ]Nozzle for encased fans
	- [ ]Analog Belt Pulley
	- [ ]Rotation Speed Controller
	- [ ]Sequenced Gearshift
	- [ ]Cuckoo Clock
	- [ ]Clockwork Bearing
	- [ ]Rope Pulley
	- [ ]Cart Assembler
	- [ ]Portable Storage Interface
	- [ ]Filters for matching a group of items or nested filters
	- [ ]Attribute Filters for matching item properties
	- [ ]Analog Lever
	- [ ]Powered Latch
	- [ ]Powered Toggle Latch
	- [ ]Sand Paper
	- [ ]Nixie Tubes
	- [ ]Extendo Grip
3. ReCreate 1.2. Corresponding to Create 0.3. Features added:
	- [ ]Mechanical Arms
	- [ ]Chute
	- [ ]Depot
	- [ ]Blaze Burners
	- [ ]Dyeable Seats
	- [ ]Mechanical Pump
	- [ ]Fluid Pipes
	- [ ]Encased and non-opaque version of the Fluid Pipe
	- [ ]Item Drain
	- [ ]Hose Pulley
	- [ ]Smart Fluid Pipe
	- [ ]Fluid Valve and dyeable valve handles
	- [ ]Fluid Tank and Creative Fluid Tank
	- [ ]Spout
	- [ ]Chocolate Bar
	- [ ]Builder's Tea
	- [ ]Minecart Couplings
	- [ ]Contraptions mounted between two Minecarts
	- [ ]Decorative wooden and metal brackets
	- [ ]Decorative Braziers
	- [ ]Portable Fluid Interface
	- [ ]Controller Rails
	- [ ]Fluids for chocolate, honey, milk and tea
	- [ ]Smart chutes
	- [ ]Gantry shafts and Gantry carriages
	- [ ]Sticker
	- [ ]Weighted Ejector
	- [ ]Honeyed apples
	- [ ]Sweet rolls
	- [ ]Chocolate glazed berries
	- [ ]Linked Controller
	- [ ]Crafting Blueprints
	- [ ]Copper Backtank
	- [ ]Diving helmet and boots
	- [ ]Precision Mechanism
	- [ ]Potato Cannon with enchantments
	- [ ]Peculiar Bell and Haunted Bell
4. ReCreate 1.3. *Here be dragons.* Possible features:
	- Integration of the Rapier physics engine
	- Backport of the **Full Steam** update

## Core backporting principles
### 1. Do not invent. Copy and adapt
ReCreate is first and foremost a backport of the Create mod. Therefore whenever you want to add a feature, you shouldn't reinvent the wheel. Most of the solution is already contained in the original code.
Want to backport a new feature? Find a corresponding class in the original repo, copy it to your ReCreate fork and then modify it until it works.
Does the class you've just added require another one? Copy it as well and repeat this process until the feature is finally working.
### 2. When in doubt - comment stuff out
While working with the original Create code intended to be run on Minecraft 1.17, you may think that some method/variable of the class you are working with is useless on 1.7.10. With that you may feel tempted to straightforward delete it. And this is when a huge mistake can be made.
Even if some method or variable seems useless right now, there might be a use for it in the future, when another feature is implemented. And when it happens, - believe me - you don't want to crawl all over the original repo looking for deleted methods and variables.
**Example:** Some classes may refer to blocks that were not yet backported (such as seats) or do not exist on 1.7.10 (such as slime blocks). Every mentioning of these blocks was commented out by me, because these blocks can either be added in the future, or implemented via an integration with [Et Futurum Requiem](http://https://github.com/GTNewHorizons/Et-Futurum-Requiem).
### 3. Different names - same functions
For seven years in development between Minecraft 1.7.10 and Minecraft 1.17.1 a lot of classes and methods had their names changed. But that doesn't mean that their functionality changed a lot. For example `World` class from 1.7.10 is called `Level` in 1.17.1, `TileEntity` was renamed to `BlockEntity`, and so on.
Eventually I'm going to make a proper Google Table document containing a list of all the renamed stuff, but for now just keep that in mind.
When trying to find a corresponding method in `Block` or `TileEntity` subclass the best way to go is to identify it by its arguments. Names were changed a lot, but the arguments are pretty much the same in all versions.
### 4. Some stuff has to go
One of the main problems of backporting is taking more advanced stuff and making it simpler. Below I will describe a few things you are most likely going to encounter.
#### 4.1. BlockState - the elephant in the room
BlockState is one of the best features of Minecraft 1.8+. Before this update one block was only able to have 16 different states, which was handled by its metadata - an integer stored along with the block. Addition of BlockStates changed that, allowing developers have virtually infinite amount of states for every block. For example, prior to 1.8 repeater and lit repeater were two different blocks, because having both 4 different lengths of delay and 4 different directions took all 16 of its possible metadata values, leaving no room for on/off state. After the 1.8. update these two blocks were merged.
So what should you do when dealing with a BlockState?
Firstly - calculate the total amount of its states. If it's 16 or lower - there's nothing to worry about. You just need to write a few methods to get block's state from its meta and vice versa. For example all the rotations of shafts and gears in ReCreate are contained within their metadata.
If the amount of states of the block is higher then 16 then one of the parameters should be moved to the block's tile entity. Such is the case for belts in ReCreate: their rotations are stored in metadata, but directions and casing material is stored in the tile entity.
#### 4.2. Models and textures
Models in future versions of Minecraft are handled via .json files, while in Minecraft they are hardcoded. Therefore the models are one of the few things that cannot be backported and has to be done from scratch. As an example you can take a look at the [original Create cogwheel model](http://https://github.com/Creators-of-Create/Create/blob/mc1.17/dev/src/main/resources/assets/create/models/block/cogwheel.json) and at [my backported version](http://https://github.com/Gordon-Frohman/ReCreate/blob/main/src/main/java/su/sergiusonesimus/recreate/content/contraptions/relays/elementary/cogwheel/CogWheelModel.java).
Same thing goes for textures: while new versions allow usage of the same texture on different faces of the model, 1.7.10 requires you to make an "unfolded" texture for all the faces.
[![Cogwheel texture from 1.17.1](http://https://github.com/Creators-of-Create/Create/blob/mc1.17/dev/src/main/resources/assets/create/textures/block/cogwheel.png "Cogwheel texture from 1.17.1")](http://https://github.com/Creators-of-Create/Create/blob/mc1.17/dev/src/main/resources/assets/create/textures/block/cogwheel.png "Cogwheel texture from 1.17.1")
[![Cogwheel texture from 1.7.10](https://github.com/Gordon-Frohman/ReCreate/blob/main/src/main/resources/assets/recreate/textures/models/cogwheel.png "Cogwheel texture from 1.7.10")](http://https://github.com/Gordon-Frohman/ReCreate/blob/main/src/main/resources/assets/recreate/textures/models/cogwheel.png "Cogwheel texture from 1.7.10")
### 5. Keep it vanilla
Create obviously requires some stuff from future Minecraft versions which isn't present in 1.7.10. We could go the easy route and just make a hard depency on Et Futurum Requiem. However I don't think every player willing to play ReCreate would want to install it. We will make the integration anyway, but it's going to be optional. ReCreate should be playable without installing any other mods (except for core ones of course).
**Example:** Minecraft 1.7.10 has no andesite, so instead of andesite alloy ReCreate will have stone synthesis, made of iron and stone. Minecraft 1.7.10 has no kelp, so belts are going to be crafted from black wool instead, and so on.
### 6. Don't be afraid to ask for help
I won't bite :smile:. You can always contact me via my Discord (sergius_0nesimus), [CurseForge PM](http://https://legacy.curseforge.com/members/sergiusonesimus/followers) or [ReCreate Issues page](http://https://github.com/Gordon-Frohman/ReCreate/issues).