/*
 * Stargate - A portal plugin for Bukkit
 * Copyright (C) 2011 Shaun (sturmeh)
 * Copyright (C) 2011 Dinnerbone
 * Copyright (C) 2011, 2012 Steven "Drakia" Scott <Contact@TheDgtl.net>
 * Copyright (C) 2017 Adam Spofford <pieflavor.mc@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
package flavor.pie.stargate;

import com.google.common.collect.Iterables;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Gate {
    public static final char ANYTHING = ' ';
    public static final char ENTRANCE = '.';
    public static final char EXIT = '*';
    private static HashMap<String, Gate> gates = new HashMap<>();
    private static HashMap<BlockState, ArrayList<Gate>> controlBlocks = new HashMap<>();
    private static HashSet<BlockState> frameBlocks = new HashSet<>();

    private Path filename;
    private char[][] layout;
    private HashMap<Character, BlockState> types;
    private RelativeBlockVector[] entrances = new RelativeBlockVector[0];
    private RelativeBlockVector[] border = new RelativeBlockVector[0];
    private RelativeBlockVector[] controls = new RelativeBlockVector[0];
    private RelativeBlockVector exitBlock = null;
    private HashMap<RelativeBlockVector, Integer> exits = new HashMap<>();
    private BlockState portalBlockOpen = BlockTypes.PORTAL.getDefaultState();
    private BlockState portalBlockClosed = BlockTypes.AIR.getDefaultState();
    
    private boolean toOwner = false;

    public Gate(Path filename, char[][] layout, HashMap<Character, BlockState> types) {
        this.filename = filename;
        this.layout = layout;
        this.types = types;

        populateCoordinates();
    }

    private void populateCoordinates() {
        ArrayList<RelativeBlockVector> entranceList = new ArrayList<>();
        ArrayList<RelativeBlockVector> borderList = new ArrayList<>();
        ArrayList<RelativeBlockVector> controlList = new ArrayList<>();
        RelativeBlockVector[] relativeExits = new RelativeBlockVector[layout[0].length];
        int[] exitDepths = new int[layout[0].length];
        RelativeBlockVector lastExit = null;

        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[y].length; x++) {
                if (layout[y][x] == '-') {
                    controlList.add(new RelativeBlockVector(x, y, 0));
                }

                if (layout[y][x] == ENTRANCE || layout[y][x] == EXIT) {
                    entranceList.add(new RelativeBlockVector(x, y, 0));
                    exitDepths[x] = y;
                    if (layout[y][x] == EXIT)
                        this.exitBlock = new RelativeBlockVector(x, y, 0);
                } else if (layout[y][x] != ANYTHING) {
                    borderList.add(new RelativeBlockVector(x, y, 0));
                }
            }
        }

        for (int x = 0; x < exitDepths.length; x++) {
            relativeExits[x] = new RelativeBlockVector(x, exitDepths[x], 0);
        }

        for (int x = relativeExits.length - 1; x >= 0; x--) {
            if (relativeExits[x] != null) {
                lastExit = relativeExits[x];
            } else {
                relativeExits[x] = lastExit;
            }

            if (exitDepths[x] > 0) this.exits.put(relativeExits[x], x);
        }

        this.entrances = entranceList.toArray(this.entrances);
        this.border = borderList.toArray(this.border);
        this.controls = controlList.toArray(this.controls);
    }
    
    public void save(Path gateFolder) throws IOException {
        try {
            BufferedWriter bw = Files.newBufferedWriter(gateFolder.resolve(filename));
            
            writeConfig(bw, "portal-open", portalBlockOpen);
            writeConfig(bw, "portal-closed", portalBlockClosed);

            for (Character type : types.keySet()) {
                BlockState value = types.get(type);
                // Skip control values

                bw.append(type);
                bw.append('=');
                bw.append(value.getId());
                bw.newLine();
            }

            bw.newLine();

            for (int y = 0; y < layout.length; y++) {
                for (int x = 0; x < layout[y].length; x++) {
                    char symbol = layout[y][x];
                    bw.append(symbol);
                }
                bw.newLine();
            }

            bw.close();
        } catch (IOException ex) {
            Stargate.log.error("Could not save Gate " + filename + " - " + ex.getMessage());
        }
    }

    private void writeConfig(BufferedWriter bw, String key, BlockState value) throws IOException {
        bw.append(String.format("%s=%s", key, value.getId()));
        bw.newLine();
    }
    
    /*private void writeConfig(BufferedWriter bw, String key, BigDecimal value) throws IOException {
        bw.append(String.format("%s=%f", key, value));
    }*/
    
    /*private void writeConfig(BufferedWriter bw, String key, boolean value) throws IOException {
        bw.append(String.format("%s=%b", key, value));
        bw.newLine();
    }*/

    public char[][] getLayout() {
        return layout;
    }
    
    public HashMap<Character, BlockState> getTypes() {
        return types;
    }

    public RelativeBlockVector[] getEntrances() {
        return entrances;
    }

    public RelativeBlockVector[] getBorder() {
        return border;
    }

    public RelativeBlockVector[] getControls() {
        return controls;
    }

    public HashMap<RelativeBlockVector, Integer> getExits() {
        return exits;
    }
    public RelativeBlockVector getExit() {
        return exitBlock;
    }

    public BlockState getControlBlock() {
        return types.get('-');
    }

    public Path getFilename() {
        return filename.getFileName();
    }

    public BlockState getPortalBlockOpen() {
        return portalBlockOpen;
    }
    
    public void setPortalBlockOpen(BlockState type) {
        portalBlockOpen = type;
    }

    public BlockState getPortalBlockClosed() {
        return portalBlockClosed;
    }
    
    public void setPortalBlockClosed(BlockState type) {
        portalBlockClosed = type;
    }
    
    public boolean getToOwner() {
        return toOwner;
    }
    
    public boolean matches(Blox topleft, int modX, int modZ) {
        return matches(topleft, modX, modZ, false);
    }

    public boolean matches(Blox topleft, int modX, int modZ, boolean onCreate) {
        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[y].length; x++) {
                BlockState id = types.get(layout[y][x]);

                if (layout[y][x] == ENTRANCE || layout[y][x] == EXIT) {
                    if (Stargate.config.portal.ignoreEntrance) continue;
                    
                    BlockState type = topleft.modRelative(x, y, 0, modX, 1, modZ).getData();
                    
                    // Ignore entrance if it's air and we're creating a new gate
                    if (onCreate && type.getType().equals(BlockTypes.AIR)) continue;
                    
                    if (!type.getType().equals(portalBlockClosed.getType()) && !type.getType().equals(portalBlockOpen.getType())) {
                        // Special case for water gates
                        if (portalBlockOpen.getType().equals(BlockTypes.FLOWING_WATER) || portalBlockOpen.getType().equals(BlockTypes.WATER)) {
                            if (type.getType().equals(BlockTypes.FLOWING_WATER) || type.getType().equals(BlockTypes.WATER)) {
                                continue;
                            }
                        }
                        if (portalBlockClosed.getType().equals(BlockTypes.FLOWING_WATER) || portalBlockClosed.getType().equals(BlockTypes.WATER)) {
                            if (type.getType().equals(BlockTypes.FLOWING_WATER) || type.getType().equals(BlockTypes.WATER)) {
                                continue;
                            }
                        }
                        // Special case for lava gates
                        if (portalBlockOpen.getType().equals(BlockTypes.FLOWING_LAVA) || portalBlockOpen.getType().equals(BlockTypes.LAVA)) {
                            if (type.getType().equals(BlockTypes.FLOWING_LAVA) || type.getType().equals(BlockTypes.LAVA)) {
                                continue;
                            }
                        }
                        if (portalBlockClosed.getType().equals(BlockTypes.FLOWING_LAVA) || portalBlockClosed.getType().equals(BlockTypes.LAVA)) {
                            if (type.getType().equals(BlockTypes.FLOWING_LAVA) || type.getType().equals(BlockTypes.LAVA)) {
                                continue;
                            }
                        }
                        Stargate.debug("Gate::Matches", "Entrance/Exit Material Mismatch: " + type);
                        return false;
                    }
                } else if (id != null) {
                    Blox mod = topleft.modRelative(x, y, 0, modX, 1, modZ);
                    if (!mod.getType().equals(id.getType())) {
                        Stargate.debug("Gate::Matches", "(" + mod.getX() + "," + mod.getY() + "," + mod.getZ() + ") Block Type Mismatch: " + mod.getType().getId() + " != " + id.getType().getId());
                        return false;
                    } else {
                        BlockState data = mod.getData();
                        if (data.supports(Keys.DIRECTION)) {
                            data = data.with(Keys.DIRECTION, id.get(Keys.DIRECTION).get()).get();
                        }
                        if (data.supports(Keys.AXIS)) {
                            data = data.with(Keys.AXIS, id.get(Keys.AXIS).get()).get();
                        }
                        if (!data.equals(id)) {
                            Stargate.debug("Gate::Matches", "(" + mod.getX() + "," + mod.getY() + "," + mod.getZ() + ") Block State Mismatch: " + data.getId() + " != " + id.getId());
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public static void registerGate(Gate gate) {
        gates.put(gate.getFilename().toString(), gate);

        BlockState blockID = gate.getControlBlock();

        if (!controlBlocks.containsKey(blockID)) {
            controlBlocks.put(blockID, new ArrayList<>());
        }

        controlBlocks.get(blockID).add(gate);
    }

    public static Gate loadGate(Path file) throws IOException {
        boolean designing = false;
        ArrayList<ArrayList<Character>> design = new ArrayList<>();
        HashMap<Character, BlockState> types = new HashMap<>();
        HashMap<String, String> config = new HashMap<>();
        HashSet<BlockState> frameTypes = new HashSet<>();
        int cols = 0;
        try (Scanner scanner = new Scanner(file)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (designing) {
                    ArrayList<Character> row = new ArrayList<>();

                    if (line.length() > cols) {
                        cols = line.length();
                    }

                    for (char symbol : line.toCharArray()) {
                        if (symbol != '.' && symbol != ' ' && symbol != '*' && ((symbol == '?') || (!types.containsKey(
                                symbol)))) {
                            Stargate.log.error(
                                    "Could not load Gate " + file.getFileName() + " - Unknown symbol '" + symbol + "' in diagram");
                            return null;
                        }
                        row.add(symbol);
                    }

                    design.add(row);
                } else {
                    if ((line.isEmpty()) || (!line.contains("="))) {
                        designing = true;
                    } else {
                        String[] split = line.split("=");
                        String key = split[0].trim();
                        String value = String.join("=", Iterables.skip(Arrays.asList(split), 1)).trim();

                        if (key.length() == 1) {
                            Character symbol = key.charAt(0);
                            BlockState id = Sponge.getRegistry().getType(BlockState.class, value).get();

                            types.put(symbol, id);
                            frameTypes.add(id);
                        } else {
                            config.put(key, value);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Stargate.log.error("Could not load Gate " + file.getFileName() + " - Invalid block ID given");
            return null;
        }

        char[][] layout = new char[design.size()][cols];

        for (int y = 0; y < design.size(); y++) {
            ArrayList<Character> row = design.get(y);
            char[] result = new char[cols];

            for (int x = 0; x < cols; x++) {
                if (x < row.size()) {
                    result[x] = row.get(x);
                } else {
                    result[x] = ' ';
                }
            }

            layout[y] = result;
        }

        Gate gate = new Gate(file, layout, types);

        gate.portalBlockOpen = readConfigBlock(config, gate, file, "portal-open", gate.portalBlockOpen);
        gate.portalBlockClosed = readConfigBlock(config, gate, file, "portal-closed", gate.portalBlockClosed);
        
        if (gate.getControls().length != 2) {
            Stargate.log.error("Could not load Gate " + file.getFileName() + " - Gates must have exactly 2 control points.");
            return null;
        }
        
        // Merge frame types, add open mat to list
        frameBlocks.addAll(frameTypes);
        
        gate.save(file.getParent()); // Updates format for version changes
        return gate;
    }

    /*private static BigDecimal readConfigNum(HashMap<String, String> config, Gate gate, Path file, String key, BigDecimal def) {
        if (config.containsKey(key)) {
            try {
                return new BigDecimal(config.get(key));
            } catch (NumberFormatException ex) {
                Stargate.log.warn(String.format("%s reading %s: %s is not numeric", ex.getClass().getName(), file, key));
            }
        }
        return def;
    }*/
    
    private static BlockState readConfigBlock(HashMap<String, String> config, Gate gate, Path file, String key, BlockState def) {
        if (config.containsKey(key)) {
            Optional<BlockState> state = Sponge.getRegistry().getType(BlockState.class, config.get(key));
            if (state.isPresent()) {
                return state.get();
            } else {
                Stargate.log.warn(String.format("Error reading %s: %s is not a BlockState (%s)", file, key, config.get(key)));
            }
        }
        return def;
    }

    public static void loadGates(Path gateFolder) throws IOException {
        List<Path> files = new LinkedList<>();
        if (Files.exists(gateFolder)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(gateFolder, path -> path.getFileName().toString().endsWith(".gate"))) {
                for (Path path : stream) {
                    files.add(path);
                }
            }
        }

        if (files.size() == 0) {
            Files.createDirectories(gateFolder);
            populateDefaults(gateFolder);
        } else {
            for (Path file : files) {
                Gate gate = loadGate(file);
                if (gate != null) registerGate(gate);
            }
        }
    }
    
    public static void populateDefaults(Path gateFolder) throws IOException {
        BlockState Obsidian = BlockTypes.OBSIDIAN.getDefaultState();
        char[][] layout = {
            {' ', 'X','X', ' '},
            {'X', '.', '.', 'X'},
            {'-', '.', '.', '-'},
            {'X', '*', '.', 'X'},
            {' ', 'X', 'X', ' '},
        };
        HashMap<Character, BlockState> types = new HashMap<>();
        types.put('X', Obsidian);
        types.put('-', Obsidian);

        Gate gate = new Gate(Paths.get("nethergate.gate"), layout, types);
        gate.save(gateFolder);
        registerGate(gate);
    }

    public static Gate[] getGatesByControlBlock(Location<World> block) {
        return getGatesByControlBlock(block.getBlock());
    }

    public static Gate[] getGatesByControlBlock(BlockState type) {
        Gate[] result = new Gate[0];
        ArrayList<Gate> lookup = controlBlocks.get(type);
        
        if (lookup != null) result = lookup.toArray(result);

        return result;
    }

    public static Gate getGateByName(String name) {
        return gates.get(name);
    }
    
    public static int getGateCount() {
        return gates.size();
    }
    
    public static boolean isGateBlock(BlockState type) {
        return frameBlocks.contains(type);
    }
    
    public static void clearGates() {
        gates.clear();
        controlBlocks.clear();
        frameBlocks.clear();
    }
}
