package com.hexagonsafio2;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Corazones: clic derecho para usarlos. Cada uno aplica su efecto al jugador. */
public class CorazonItem extends DescItem {
	public interface Efecto {
		void aplicar(ServerPlayer jugador);
	}

	private static final ResourceLocation MODIFICADOR = NaveCraft.id("corazones_extra");
	private final Efecto efecto;

	public CorazonItem(Properties properties, Efecto efecto) {
		super(properties);
		this.efecto = efecto;
	}

	/** Suma corazones a la vida maxima. Se pierden al morir (el jugador nuevo no los hereda). */
	public static void masCorazones(ServerPlayer jugador, int corazones) {
		AttributeInstance vida = jugador.getAttribute(Attributes.MAX_HEALTH);
		if (vida == null) return;
		AttributeModifier anterior = vida.getModifier(MODIFICADOR);
		double total = (anterior == null ? 0.0 : anterior.amount()) + corazones * 2.0;
		vida.addOrReplacePermanentModifier(new AttributeModifier(MODIFICADOR, total, AttributeModifier.Operation.ADD_VALUE));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide && player instanceof ServerPlayer jugador) {
			efecto.aplicar(jugador);
			level.playSound(null, jugador.getX(), jugador.getY(), jugador.getZ(),
					SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.3F);
			if (!player.getAbilities().instabuild) stack.shrink(1);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
