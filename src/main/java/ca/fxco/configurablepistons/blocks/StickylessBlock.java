package ca.fxco.configurablepistons.blocks;

import ca.fxco.configurablepistons.pistonLogic.StickyType;
import ca.fxco.configurablepistons.pistonLogic.accessible.ConfigurablePistonStickiness;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.Map;

public class StickylessBlock extends Block implements ConfigurablePistonStickiness {

    private static final Map<Direction, StickyType> STICKYLESS_LIST = new HashMap<>() {{
        put(Direction.UP, StickyType.NO_STICK);
        put(Direction.DOWN, StickyType.NO_STICK);
        put(Direction.NORTH, StickyType.NO_STICK);
        put(Direction.SOUTH, StickyType.NO_STICK);
        put(Direction.EAST, StickyType.NO_STICK);
        put(Direction.WEST, StickyType.NO_STICK);
    }};

    public StickylessBlock(Settings settings) {
        super(settings);
    }

    @Override
    public boolean usesConfigurablePistonStickiness() {
        return true;
    }

    @Override // Might try setting this to false, and not using the list. Should work the same
    public boolean isSticky(BlockState state) {
        return true;
    }

    @Override
    public Map<Direction, StickyType> stickySides(BlockState state) {
        return STICKYLESS_LIST;
    }

    @Override
    public StickyType sideStickiness(BlockState state, Direction direction) {
        return StickyType.NO_STICK;
    }
}
