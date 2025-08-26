# Minecraft Commercialize

A highly integrated commerce mod for Minecraft. Introduces a server-wide goods market of player-created and procedurally generated item offers, a shipping box to auto-sell items on a configurable interval (like the Stardew Valley Shipping Bin), and a posting box to post items to the market for other players to buy. Integrates deeply with [*Create: Numismatics*](https://modrinth.com/mod/numismatics) and [*MrCrayfish's Furniture Refurbished](https://www.curseforge.com/minecraft/mc-mods/refurbished-furniture) to offer cash and card payments and deposits, and an advanced mail and delivery system. This mod is an original work and a collaborative project created by Saint with custom model and texture work by Mezu. 

Developed for Saint's [Project Haze](https://haze.blockworlds.io/), a finely rebalanced survival mod pack with a vision.

# Features

The mod integrates with its sibling mods to provide the following features:

- 🖥️ The *Market Terminal* gives access to a server-wide marketplace
	- Browse, filter, and search all offers available on market (name, price, expiration, seller, …)
	- Switch between payment with coins in inventory or from your *Numismatics* bank account (configurable)
	- Pay with specific *Numismatics* bank cards bound to a player's account
	- A custom, detailed 1x2 model, textures, and a hand-made GUI (red terminal)

- 🎲 *Procedurally generated market offers* to simulate a broader player economy
	- Offer templates define what kind of offers can be generated (item type, stack sizes, pricing)
	- Configurable generation interval and buying/selling price difference (configurable)

- 📫 Receive market orders as *physical mail* to your *Furniture Refurbished* mailbox
	- Orders are placed into a transit queue and delivered after a time delay (configurable)
	- If a player does not have a valid mailbox, multiple delivery attempts are made (configurable)
	- If a player has multiple mailboxes, one can be marked as a main recipient (configurable)
	- Delivery attempts are suspended for players that are offline (configurable)

- 📤 The *Shipping Box* allows players to deposit items and sell en masse
	- Stores items to be sold, auto-sell on an interval (configurable)
	- Goods are exchanged for cash value by default, deposited directly into the box
	- The box optionally accepts a *Numismatics* account card to deposit funds into a bank account
	- A custom, detailed 1x2 model, textures, and a hand-made GUI ([blue box](https://www.youtube.com/watch?v=75V4ClJZME4))

- 📌 The *Posting Box* allows players to post their own offers to the market
	- Create orders for any item stack with a custom price and duration
	- Post as a single stack or as individual offers in bulk (e.g. for consumables or rare goods)
	- A custom, detailed 1x2 model, textures, and a hand-made GUI (red box)

- 🧾 Extensive configuration and customization options
	- In-game server and client configuration (via mod menu)
	- Item value files to define an exact currency value for any item in the game (with defaults)
	- Offer template files to define item types, rarity, pricing (with defaults)

- 🔊 Custom world sound effects for block interaction (buying, selling, posting)
	- Added sound effect when receiving mail in *Furniture Refurbished* mail box

# Integration

- [Consumable Tooltips](https://github.com/AugustSaintFreytag/minecraft-consumable-tooltips) for displaying base value in tooltips
- [Create: Numismatics](https://modrinth.com/mod/numismatics) for cash currency and banking
- [Furniture Refurbished](https://www.curseforge.com/minecraft/mc-mods/refurbished-furniture) for personal mail delivery

# License

This project is available under the Attribution-NonCommercial-NoDerivatives 4.0 International License (CC BY-NC-ND 4.0). You may share and redistribute the material in any medium or format, provided that proper credit is given. Commercial use and the creation of derivative works are not permitted. The project integrates with but does not use any code or resources from: [Create](https://modrinth.com/mod/create-fabric) by Tropheusj, [Create: Numismatics](https://modrinth.com/mod/numismatics) by IThundxr, [Furniture Refurbished](https://www.curseforge.com/minecraft/mc-mods/refurbished-furniture) by MrCrayfish. Graphical user interfaces are built using [owo-lib](https://modrinth.com/mod/owo-lib) by WispForest.