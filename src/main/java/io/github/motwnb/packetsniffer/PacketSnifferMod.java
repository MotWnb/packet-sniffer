package io.github.motwnb.packetsniffer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class PacketSnifferMod implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		PacketLogger.init(FabricLoader.getInstance().getGameDir());
		Runtime.getRuntime().addShutdownHook(new Thread(PacketLogger::shutdown));
	}
}
