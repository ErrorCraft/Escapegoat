package net.errorcraft.escapegoat.mixin.nbt;

import net.errorcraft.escapegoat.StringEscapers;
import net.minecraft.nbt.NbtString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(NbtString.class)
public class NbtStringExtender {
    /**
     * @author ErrorCraft
     * @reason Uses a StringEscaper for more versatile strings.
     */
    @Overwrite
    public static String escape(String value) {
        return StringEscapers.SNBT.escape(value);
    }
}
