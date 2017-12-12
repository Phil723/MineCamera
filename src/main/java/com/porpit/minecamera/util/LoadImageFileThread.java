package com.porpit.minecamera.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.porpit.minecamera.MineCamera;
import com.porpit.minecamera.network.MessageImageRequest;
import com.porpit.minecamera.network.NetworkLoader;

import net.minecraft.client.Minecraft;

public class LoadImageFileThread extends Thread {
	private String imagename;

	public LoadImageFileThread(String imagename) {
		this.imagename = imagename;
	}

	public void run() {
		BufferedImage image = null;
		File file = new File(MineCamera.ClientCatchFile + imagename + ".jpeg");
		if (file.exists()) {
			// System.out.println("ͼƬ����");
			try {
				image = ImageIO.read(file);
			} catch (IOException e) {
				//����ͼƬ������Ҫ�����ͻ������¼���
				PictureFactory.fildToLoadPicture.put("imagename", EnumFailLoadImage.ErrorToLoadLocalFile);
				file.delete();
				e.printStackTrace();
			}
			if (image != null) {
				final BufferedImage imageIn = image;
				Minecraft.getMinecraft().addScheduledTask(new Runnable() {
					@Override
					public void run() {
						int textureId = PictureFactory.loadTexture(imagename, imageIn);
						PictureFactory.loadedPicture.put(imagename, textureId);
						PictureFactory.lodingPicture.remove(imagename);
					}
				});
			} else {
				// System.out.println("image==null");
			}
		} else {
			// System.out.println("����");
			MessageImageRequest message = new MessageImageRequest();
			message.filename = imagename;
			NetworkLoader.instance.sendToServer(message);
		}
	}
}