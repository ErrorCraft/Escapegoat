# Escapegoat
Escapegoat is a *Fabric* mod for *Minecraft* that adds various escape sequences to [SNBT](https://minecraft.wiki/w/NBT_format#SNBT_format) strings.
This mod was made because of the change made in [snapshot 25w02a](https://www.minecraft.net/en-us/article/minecraft-snapshot-25w02a) regarding text components in commands like `tellraw`, because they now use SNBT instead of JSON.
Because of this change, some functionality was lost, such as the ability to escape most characters.
This mod is there to fix that by adding the escape functionality to SNBT.
See the bug reports [MC-279229](https://bugs.mojang.com/browse/MC-279229) and [MC-279250](https://bugs.mojang.com/browse/MC-279250) for more information.

Escape sequences work both ways, for input and for output, though note that stringifying data only works with NBT.
This is something that is **very** likely to change in the future to make it independent of NBT, either by *Mojang* or by me in this mod, so please watch out for that.

![A tellraw command with an escape sequence.](/img/tellraw_command.png)

## Examples
- `/tellraw @a "Mi\necraft"`
- `/tellraw @a {text: 'Abracadabra: \u{2728}', bold: true, color: "yellow"}`
- `/item modify entity @s weapon {function: 'minecraft:set_writable_book_pages', pages: ['Line 1\nLine 2\nLine 3'], mode: 'replace_all'}`

## Escape Sequences
All escape sequences start with the `\` character.

### Simple escape sequences
- `\\`: Backslash
- `\'`: Single quote (context-dependent, see below)
- `\"`: Double quote (context-dependent, see below)
- `\n`: Line feed

Other common simple escape sequences are currently not allowed [due to *Minecraft* not rendering them properly](https://bugs.mojang.com/browse/MC-278221).

Escape sequences for surrounders are only allowed when used in a string with that surrounder.
This means that `'` and `"` may be escaped depending on which one is used for a string that is surrounded by that character.
In other words: `'\''` is valid, but `'\"'` is not, and vice versa, just like how it works in *Minecraft* already.

### Complicated escape sequences
These complicated escape sequences can be used to represent any *Unicode* character.
- `\uXXXX`: A fixed-length four-character hexadecimal UTF-16 character.
  The same as the one used in languages like JSON and JavaScript. \
  Examples:
  - `\u004D` (M)
  - `\u2605` (★)
  - `\uD83D\uDE0A` (😊)
- `\u{XXXXXX}`: A variable-length hexadecimal UTF-32 character.
  The same as the one used in JavaScript. \
  Examples:
  - `\u{4D}` (M)
  - `\u{2605}` (★)
  - `\u{1F60A}` (😊)

![A tellraw command with the above complicated escape sequences.](/img/complicated_escape_sequences.png)
