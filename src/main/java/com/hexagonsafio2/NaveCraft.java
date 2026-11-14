package com.hexagonsafio2;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Objetos y crafteos para armar la Nave Espacial (la nave en si viene del mod ghostsafio). */
public class NaveCraft implements ModInitializer {
	public static final String MOD_ID = "hexagonsafio2";

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	private static Item item(String name) {
		return Registry.register(BuiltInRegistries.ITEM, id(name), new DescItem(new Item.Properties()));
	}

	private static Item corazon(String name, CorazonItem.Efecto efecto) {
		return Registry.register(BuiltInRegistries.ITEM, id(name), new CorazonItem(new Item.Properties().stacksTo(16), efecto));
	}

	public static final ResourceLocation NAVE_ID = ResourceLocation.fromNamespaceAndPath("ghostsafio", "nave");

	/** Plataforma donde se coloca la nave (la nave solo se puede poner sobre ella). */
	public static final Block PLATAFORMA_NAVE = Registry.register(BuiltInRegistries.BLOCK, id("plataforma_nave"),
			new Block(BlockBehaviour.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL)));
	public static final Item PLATAFORMA_NAVE_ITEM = Registry.register(BuiltInRegistries.ITEM, id("plataforma_nave"),
			new BlockItem(PLATAFORMA_NAVE, new Item.Properties()));

	public static final Item DILITIO = item("dilitio");
	public static final Item AMBAR_FOSIL = item("ambar_fosil");
	public static final Item BIOXITA = item("bioxita");
	public static final Item FRASCO_DILITIO = item("frasco_dilitio");
	public static final Item CABINA_NAVE = item("cabina_nave");
	public static final Item ALA_IZQUIERDA_NAVE = item("ala_izquierda_nave");
	public static final Item ALA_DERECHA_NAVE = item("ala_derecha_nave");
	public static final Item FUSELAJE_NAVE = item("fuselaje_nave");
	/** Rojo: 1 corazon extra. */
	public static final Item CORAZON = corazon("corazon", j -> {
		CorazonItem.masCorazones(j, 1);
		j.heal(2.0F);
	});
	/** Limbo: cura toda la vida y da 3 corazones extra. */
	public static final Item CORAZON_LIMBO = corazon("corazon_limbo", j -> {
		CorazonItem.masCorazones(j, 3);
		j.setHealth(j.getMaxHealth());
	});
	/** Amarillo: 5 corazones amarillos (absorcion). */
	public static final Item CORAZON_AMARILLO = corazon("corazon_amarillo", j ->
		j.setAbsorptionAmount(j.getAbsorptionAmount() + 10.0F));

	public static final Item MOTOR_NAVE = item("motor_nave");
	public static final Item MOTOR_MEJORADO_NAVE = item("motor_mejorado_nave");

	public static final ResourceLocation ARCO_ID = ResourceLocation.fromNamespaceAndPath("ghostsafio", "arco_glebanoide");
	/** Dano extra de las flechas del Arco Glebanoide (equivale a Poder III-IV). */
	private static final double PODER_EXTRA = 2.25;

	@Override
	public void onInitialize() {
		// el Arco Glebanoide pega mas fuerte
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof AbstractArrow flecha && flecha.getOwner() instanceof LivingEntity tirador) {
				ItemStack arco = tirador.getUseItem();
				if (BuiltInRegistries.ITEM.getKey(arco.getItem()).equals(ARCO_ID)) {
					flecha.setBaseDamage(flecha.getBaseDamage() + PODER_EXTRA);
				}
			}
		});

		// la nave solo se puede colocar sobre la cara de arriba de la plataforma
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(NAVE_ID)) return InteractionResult.PASS;
			boolean sobrePlataforma = hit.getDirection() == Direction.UP
					&& level.getBlockState(hit.getBlockPos()).is(PLATAFORMA_NAVE);
			if (sobrePlataforma) return InteractionResult.PASS;
			if (!level.isClientSide) {
				player.displayClientMessage(Component.translatable("message.hexagonsafio2.necesita_plataforma"), true);
			}
			return InteractionResult.FAIL;
		});

		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("nave"),
				FabricItemGroup.builder()
						.title(Component.translatable("itemGroup.hexagonsafio2.nave"))
						.icon(() -> new ItemStack(MOTOR_NAVE))
						.displayItems((params, out) -> {
							out.accept(PLATAFORMA_NAVE_ITEM);
							out.accept(DILITIO);
							out.accept(AMBAR_FOSIL);
							out.accept(BIOXITA);
							out.accept(FRASCO_DILITIO);
							out.accept(CABINA_NAVE);
							out.accept(ALA_IZQUIERDA_NAVE);
							out.accept(ALA_DERECHA_NAVE);
							out.accept(FUSELAJE_NAVE);
							out.accept(MOTOR_NAVE);
							out.accept(MOTOR_MEJORADO_NAVE);
							out.accept(CORAZON);
							out.accept(CORAZON_LIMBO);
							out.accept(CORAZON_AMARILLO);
							// la nave que viene de ghostsafio
							Item nave = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("ghostsafio", "nave"));
							if (nave != Items.AIR) out.accept(nave);
						})
						.build());
	}
}
