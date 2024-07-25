package io.github.mcmodknower.veryblueworld.mixin.client;

import net.minecraft.resource.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
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
		byte[] original;
		try(InputStream in = cir.getReturnValue()) {
			ByteArrayOutputStream originalCopying = new ByteArrayOutputStream(in.available());
			in.transferTo(originalCopying);
			original = originalCopying.toByteArray();
		}

		BufferedImage image = ImageIO.read(new ByteArrayInputStream(original));

		// if the input stream is not of a known image type,
		// ImageIO#read returns null
		if(image == null) {
			cir.setReturnValue(new ByteArrayInputStream(original));
			return;
		}


		// Copy to a well editable image
		int height = image.getHeight();
		int width = image.getWidth();
		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = outputImage.createGraphics();
		graphics.drawRenderedImage(image, new AffineTransform());

		// paint it blue, but keep the old alpha
		graphics.setComposite(AlphaComposite.SrcAtop);
		graphics.setColor(Color.BLUE);
		graphics.fillRect(0, 0, width, height);
		graphics.dispose();

		// Wrap the result, so it can be return as an InputStream
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ImageIO.write(outputImage, "png", byteOut);
		byte[] data = byteOut.toByteArray();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		cir.setReturnValue(inputStream);
	}
}