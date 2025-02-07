package net.errorcraft.escapegoat.mixin.nbt;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.errorcraft.escapegoat.StringEscapers;
import net.errorcraft.escapegoat.UnescapeContext;
import net.errorcraft.escapegoat.UnescapeStringException;
import net.minecraft.nbt.StringNbtReader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StringNbtReader.class)
public class StringNbtReaderExtender {
    @Unique
    private static final DynamicCommandExceptionType ESCAPE_EXCEPTION = new DynamicCommandExceptionType(text -> new LiteralMessage(text.toString()));

    @Shadow
    @Final
    private StringReader reader;

    @Unique
    private final UnescapeContext context = new UnescapeContext(false, (readCodePoints, readChars) -> this.reader.setCursor(this.reader.getCursor() + readChars));

    @Redirect(
        method = "parsePrimitive",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/brigadier/StringReader;readQuotedString()Ljava/lang/String;",
            remap = false
        )
    )
    private String useStringEscaperInstead(StringReader instance) throws CommandSyntaxException {
        try {
            return StringEscapers.SNBT.unescape(this.reader.getRemaining(), this.context);
        } catch (UnescapeStringException e) {
            throw ESCAPE_EXCEPTION.createWithContext(this.reader, e.getMessage());
        }
    }
}
