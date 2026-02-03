package net.minecraft.world.gen;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.List;
import java.util.Map;

public class FlatGeneratorInfo {
    private final List<FlatLayerInfo> flatLayers = Lists.newArrayList();
    private final Map<String, Map<String, String>> worldFeatures = Maps.newHashMap();
    private int biomeToUse;

    /**
     * Return the biome used on this preset.
     */
    public int getBiome() {
        return this.biomeToUse;
    }

    /**
     * Set the biome used on this preset.
     */
    public void setBiome(int biome) {
        this.biomeToUse = biome;
    }

    public Map<String, Map<String, String>> getWorldFeatures() {
        return this.worldFeatures;
    }

    public List<FlatLayerInfo> getFlatLayers() {
        return this.flatLayers;
    }

    public void func_82645_d() {
        int i = 0;

        for (FlatLayerInfo flatlayerinfo : this.flatLayers) {
            flatlayerinfo.setMinY(i);
            i += flatlayerinfo.getLayerCount();
        }
    }

    public String toString() {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(3);
        stringbuilder.append(";");

        for (int i = 0; i < this.flatLayers.size(); ++i) {
            if (0 < i) {
                stringbuilder.append(",");
            }

            stringbuilder.append(this.flatLayers.get(i).toString());
        }

        stringbuilder.append(";");
        stringbuilder.append(this.biomeToUse);

        if (!this.worldFeatures.isEmpty()) {
            stringbuilder.append(";");
            int k = 0;

            for (final Map.Entry<String, Map<String, String>> entry : worldFeatures.entrySet()) {
                if (0 < k++) {
                    stringbuilder.append(",");
                }

                stringbuilder.append(entry.getKey().toLowerCase());
                final Map<String, String> map = entry.getValue();

                if (!map.isEmpty()) {
                    stringbuilder.append("(");
                    int j = 0;

                    for (Map.Entry<String, String> entry1 : map.entrySet()) {
                        if (0 < j++) {
                            stringbuilder.append(" ");
                        }

                        stringbuilder.append(entry1.getKey());
                        stringbuilder.append("=");
                        stringbuilder.append(entry1.getValue());
                    }

                    stringbuilder.append(")");
                }
            }
        } else {
            stringbuilder.append(";");
        }

        return stringbuilder.toString();
    }

    private static FlatLayerInfo func_180715_a(int p_180715_0_, String p_180715_1_, int p_180715_2_) {
        String[] astring = 3 <= p_180715_0_ ? p_180715_1_.split("\\*", 2) : p_180715_1_.split("x", 2);
        int i = 1;
        int j = 0;

        if (2 == astring.length) {
            try {
                i = Integer.parseInt(astring[0]);

                if (256 <= p_180715_2_ + i) {
                    i = 256 - p_180715_2_;
                }

                if (0 > i) {
                    i = 0;
                }
            } catch (Throwable var8) {
                return null;
            }
        }

        Block block = null;

        try {
            String s = astring[astring.length - 1];

            if (3 > p_180715_0_) {
                astring = s.split(":", 2);

                if (1 < astring.length) {
                    j = Integer.parseInt(astring[1]);
                }

                block = Block.getBlockById(Integer.parseInt(astring[0]));
            } else {
                astring = s.split(":", 3);
                block = 1 < astring.length ? Block.getBlockFromName(astring[0] + ":" + astring[1]) : null;

                if (null != block) {
                    j = 2 < astring.length ? Integer.parseInt(astring[2]) : 0;
                } else {
                    block = Block.getBlockFromName(astring[0]);

                    if (null != block) {
                        j = 1 < astring.length ? Integer.parseInt(astring[1]) : 0;
                    }
                }

                if (null == block) {
                    return null;
                }
            }

            if (block == Blocks.air) {
                j = 0;
            }

            if (0 > j || 15 < j) {
                j = 0;
            }
        } catch (Throwable var9) {
            return null;
        }

        FlatLayerInfo flatlayerinfo = new FlatLayerInfo(p_180715_0_, i, block, j);
        flatlayerinfo.setMinY(p_180715_2_);
        return flatlayerinfo;
    }

    private static List<FlatLayerInfo> func_180716_a(int p_180716_0_, String p_180716_1_) {
        if (null != p_180716_1_ && 1 <= p_180716_1_.length()) {
            List<FlatLayerInfo> list = Lists.newArrayList();
            String[] astring = p_180716_1_.split(",");
            int i = 0;

            for (String s : astring) {
                FlatLayerInfo flatlayerinfo = func_180715_a(p_180716_0_, s, i);

                if (null == flatlayerinfo) {
                    return null;
                }

                list.add(flatlayerinfo);
                i += flatlayerinfo.getLayerCount();
            }

            return list;
        } else {
            return null;
        }
    }

    public static FlatGeneratorInfo createFlatGeneratorFromString(String flatGeneratorSettings) {
        if (null == flatGeneratorSettings) {
            return getDefaultFlatGenerator();
        } else {
            String[] astring = flatGeneratorSettings.split(";", -1);
            int i = 1 == astring.length ? 0 : MathHelper.parseIntWithDefault(astring[0], 0);

            if (0 <= i && 3 >= i) {
                FlatGeneratorInfo flatgeneratorinfo = new FlatGeneratorInfo();
                int j = 1 == astring.length ? 0 : 1;
                List<FlatLayerInfo> list = func_180716_a(i, astring[j++]);

                if (null != list && !list.isEmpty()) {
                    flatgeneratorinfo.getFlatLayers().addAll(list);
                    flatgeneratorinfo.func_82645_d();
                    int k = BiomeGenBase.plains.biomeID;

                    if (0 < i && astring.length > j) {
                        k = MathHelper.parseIntWithDefault(astring[j++], k);
                    }

                    flatgeneratorinfo.setBiome(k);

                    if (0 < i && astring.length > j) {
                        String[] astring1 = astring[j++].toLowerCase().split(",");

                        for (String s : astring1) {
                            String[] astring2 = s.split("\\(", 2);
                            Map<String, String> map = Maps.newHashMap();

                            if (0 < astring2[0].length()) {
                                flatgeneratorinfo.getWorldFeatures().put(astring2[0], map);

                                if (1 < astring2.length && astring2[1].endsWith(")") && 1 < astring2[1].length()) {
                                    String[] astring3 = astring2[1].substring(0, astring2[1].length() - 1).split(" ");

                                    for (int l = 0; l < astring3.length; ++l) {
                                        String[] astring4 = astring3[l].split("=", 2);

                                        if (2 == astring4.length) {
                                            map.put(astring4[0], astring4[1]);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        flatgeneratorinfo.getWorldFeatures().put("village", Maps.newHashMap());
                    }

                    return flatgeneratorinfo;
                } else {
                    return getDefaultFlatGenerator();
                }
            } else {
                return getDefaultFlatGenerator();
            }
        }
    }

    public static FlatGeneratorInfo getDefaultFlatGenerator() {
        FlatGeneratorInfo flatgeneratorinfo = new FlatGeneratorInfo();
        flatgeneratorinfo.setBiome(BiomeGenBase.plains.biomeID);
        flatgeneratorinfo.getFlatLayers().add(new FlatLayerInfo(1, Blocks.bedrock));
        flatgeneratorinfo.getFlatLayers().add(new FlatLayerInfo(2, Blocks.dirt));
        flatgeneratorinfo.getFlatLayers().add(new FlatLayerInfo(1, Blocks.grass));
        flatgeneratorinfo.func_82645_d();
        flatgeneratorinfo.getWorldFeatures().put("village", Maps.newHashMap());
        return flatgeneratorinfo;
    }
}