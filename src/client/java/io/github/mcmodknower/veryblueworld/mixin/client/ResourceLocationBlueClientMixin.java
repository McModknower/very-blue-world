package io.github.mcmodknower.veryblueworld.mixin.client;

import net.minecraft.resource.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Mixin(Resource.class)
public abstract class ResourceLocationBlueClientMixin {
	@Inject(at = @At("RETURN"), method = "getInputStream()Ljava/io/InputStream;", cancellable = true)
	private void getInputStream(CallbackInfoReturnable<InputStream> cir) throws IOException {

		// Copy the data of the input stream so mc can get the original data
		// when it is not an image
		InputStream in = cir.getReturnValue();
		ByteArrayOutputStream originalCopying = new ByteArrayOutputStream(in.available());
		in.transferTo(originalCopying);
		byte[] original = originalCopying.toByteArray();

		BufferedImage image = ImageIO.read(new ByteArrayInputStream(original));

		// if the input stream is not of a known image type,
		// ImageIO#read returns null
		if(image == null) {
			cir.setReturnValue(new ByteArrayInputStream(original));
			return;
		}

		// Paint it blue
		int height = image.getHeight();
		int width = image.getWidth();
		Graphics2D graphics = image.createGraphics();
		graphics.setBackground(Color.BLUE);
		graphics.clearRect(0,0,width,height);
		graphics.dispose();

		// Wrap the result, so it can be return as an InputStream
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ImageIO.write(image, "png", byteOut);
		byte[] data = byteOut.toByteArray();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		cir.setReturnValue(inputStream);
	}
}