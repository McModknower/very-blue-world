package io.github.mcmodknower.veryblueworld.mixin.client;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;

@Mixin(Resource.class)
public abstract class ResourceLocationBlueClientMixin {

	@Unique
	private static final int BLUE = 0xFF;
	@Unique
	private static final int ALPHA_MASK = 0xFF << 24;

	@Inject(at = @At("RETURN"), method = "getInputStream()Ljava/io/InputStream;", cancellable = true)
	private void getInputStream(CallbackInfoReturnable<InputStream> cir) throws IOException {

		// Copy the data of the input stream so mc can get the original data
		// when it is not an image
		byte[] original;
		try(InputStream in = cir.getReturnValue()) {
			ByteArrayOutputStream originalCopying = new ByteArrayOutputStream(in.available());
            in.transferTo(originalCopying);
			original = originalCopying.toByteArray();
		}

		try(NativeImage image = NativeImage.read(new ByteArrayInputStream(original))) {
			int width = image.getWidth();
			int height = image.getHeight();
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int color = image.getColorArgb(x,y);
					image.setColorArgb(x,y,(color & ALPHA_MASK) | BLUE);
				}
			}

            //ASSUMPTION: java will clean these files up fast enough
            Path tmpFile = Files.createTempFile("very-blue-world", null);
            image.writeTo(tmpFile);
            cir.setReturnValue(Files.newInputStream(tmpFile, StandardOpenOption.DELETE_ON_CLOSE));
		} catch (IOException e) {
			cir.setReturnValue(new ByteArrayInputStream(original));
		}
	}

}