package provider;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.ReentrantLock;
import org.nfunk.jep.JEP;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import provider.wz.MapleDataType;
import tools.ObjectParser;

public class MapleDataTool {

    public static String getString(MapleData data) {
        if (data == null) {
            return null;
        }
        Object d = data.getData();
        if (d == null) {
            return null;
        }
        return ((String) d);
    }

    public static String getString(MapleData data, String def) {
        if (data == null || data.getData() == null) {
            return def;
        } else {
            return ((String) data.getData());
        }
    }

    public static String getString(String path, MapleData data) {
        return getString(data.getChildByPath(path));
    }

    public static String getString(String path, MapleData data, String def) {
        if (path == null || data == null) {
            return def;
        }
        MapleData d = data.getChildByPath(path);
        if (d == null) {
            return def;
        }
        return getString(d, def);
    }

    public static double getDouble(MapleData data) {
        if (data == null || data.getData() == null) {
            return 0;
        }
        if (data.getType().equals(MapleDataType.STRING)) {
            Double in = ObjectParser.isDouble(getString(data));
            if (in == null) {
                return 0;
            } else {
                return in;
            }
        }
        Double in = ((Double) data.getData());
        if (in == null) {
            return 0;
        } else {
            return in.doubleValue();
        }
    }

    public static double getDouble(String path, MapleData data) {
        return getDouble(data.getChildByPath(path));
    }

    public static float getFloat(MapleData data) {
        if (data == null || data.getData() == null) {
            return 0;
        }
        if (data.getType().equals(MapleDataType.STRING)) {
            Float in = ObjectParser.isFloat(getString(data));
            if (in == null) {
                return 0;
            } else {
                return in;
            }
        } else if (data.getType().equals(MapleDataType.DOUBLE)) {
            return (float) getDouble(data);
        }
        Float in = ((Float) data.getData());
        if (in == null) {
            return 0;
        } else {
            return in.floatValue();
        }
    }

    public static float getFloat(String path, MapleData data, float def) {
        if (path == null || data == null) {
            return def;
        }
        MapleData d = data.getChildByPath(path);
        if (d == null) {
            return def;
        }
        return getFloat(d, def);
    }

    public static float getFloat(String path, MapleData data) {
        return getFloat(data.getChildByPath(path));
    }

    public static float getFloat(MapleData data, float def) {
        if (data == null || data.getData() == null) {
            return def;
        } else if (data.getType() == MapleDataType.STRING) {
            try {
                return Float.parseFloat(getString(data));
            } catch (NumberFormatException nfe) {
                return def;
            }
        } else {
            Float in = ((Float) data.getData());
            if (in == null) {
                return def;
            } else {
                return in.floatValue();
            }
        }
    }

    public static int getInt(MapleData data) {
        if (data == null || data.getData() == null) {
            return 0;
        }
        if (data.getType().equals(MapleDataType.STRING)) {
            Integer in = ObjectParser.isInt(getString(data));
            if (in == null) {
                return 0;
            } else {
                return in;
            }
        }
        Integer in = ((Integer) data.getData());
        if (in == null) {
            return 0;
        } else {
            return in.intValue();
        }
    }

    public static int getInt(String path, MapleData data) {
        return getInt(data.getChildByPath(path));
    }

    public static int getInt(String path, MapleData data, int def, int level) {
        return getInt(data.getChildByPath(path), def, level);
    }

    public static int getIntConvert(MapleData data) {
        if (data == null) {
            return 0;
        }
        if (data.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(data));
        } else {
            return getInt(data);
        }
    }

    public static int getIntConvert(String path, MapleData data) {
        MapleData d = data.getChildByPath(path);
        if (d.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(d));
        } else {
            return getInt(d);
        }
    }

    public static int getInt(MapleData data, int def) {
        if (data == null || data.getData() == null) {
            return def;
        } else if (data.getType() == MapleDataType.STRING) {
            try {
                return Integer.parseInt(getString(data));
            } catch (NumberFormatException nfe) {
                return def;
            }
        } else if (data.getType() == MapleDataType.SHORT) {
            return getShort(data, (short) def);
        } else {
            Integer in = ((Integer) data.getData());
            if (in == null) {
                return def;
            } else {
                return in.intValue();
            }
        }
    }

    public static int getInt(String path, MapleData data, int def) {
        if (data == null) {
            return def;
        }
        MapleData d = data.getChildByPath(path);
        if (d == null) {
            return def;
        }
        return getInt(d, def);
    }

    public static int getInt(MapleData data, int def, int skillLevel) {
        if (data == null) {
            return def;
        } else if (data.getType() == MapleDataType.STRING) {
            int val = parseSkillInfo(getString(data), skillLevel);
            return val;
        } else {
            return ((Integer) data.getData()).intValue();
        }
    }

    public static int getIntConvert(String path, MapleData data, int def) {
        if (data == null) {
            return def;
        }
        MapleData d = data.getChildByPath(path);
        if (d == null) {
            return def;
        }
        return getInt(d, def);
    }

    public static int getIntConvert(String path, MapleData data, int def, int skillLevel) {
        if (data == null) {
            return def;
        }
        MapleData d = data.getChildByPath(path);
        if (d == null) {
            return def;
        }
        if (d.getType() == MapleDataType.STRING) {
            try {
                return parseSkillInfo(getString(d), skillLevel);
            } catch (NumberFormatException nfe) {
                return def;
            }
        } else {
            return getInt(d, def, skillLevel);
        }
    }

    public static short getShort(MapleData data) {
        return getShort(data, (short) 0);
    }

    public static short getShort(MapleData data, short def) {
        if (data == null || data.getData() == null) {
            return def;
        }
        if (data.getType() == MapleDataType.STRING) {
            try {
                return Short.parseShort(getString(data));
            } catch (NumberFormatException nfe) {
                return def;
            }
        } else if (data.getType() == MapleDataType.SHORT) {
            Short in = ((Short) data.getData());
            if (in == null) {
                return def;
            } else {
                return in.shortValue();
            }
        } else if (data.getType() == MapleDataType.INT) {
            Integer in = ((Integer) data.getData());
            if (in == null) {
                return def;
            } else {
                return in.shortValue();
            }
        } else {
            System.out.println("Trying to get a short when its: " + data.getType().name());
            return def;
        }
    }

    public static BufferedImage getImage(MapleData data) {
        return ((MapleCanvas) data.getData()).getImage();
    }

    public static Point getPoint(MapleData data) {
        if (data == null) {
            return null;
        }
        Object point = data.getData();
        if (point != null) {
            return ((Point) point);
        } else {
            return null;
        }
    }

    public static Point getPoint(String path, MapleData data) {
        return getPoint(data.getChildByPath(path));
    }

    public static Point getPoint(String path, MapleData data, Point def) {
        final MapleData pointData = data.getChildByPath(path);
        if (pointData == null) {
            return def;
        }
        return getPoint(pointData);
    }

    public static String getFullDataPath(MapleData data) {
        String path = "";
        MapleDataEntity myData = data;
        while (myData != null) {
            path = myData.getName() + "/" + path;
            myData = myData.getParent();
        }
        return path.substring(0, path.length() - 1);
    }

    private final static JEP exp = new JEP();
    private static boolean variablesSet = false;
    final static ReentrantLock lock = new ReentrantLock();

    public static int parseSkillInfo(String equation, int skillLevel) {
        int endResult = 1;
        // meaning there is no calculation to do
        if (!equation.contains("*") && !equation.contains("+") && !equation.contains("-") && !equation.contains("/") && !equation.contains("d") && !equation.contains("u") && !equation.contains("x")) {
            return Integer.parseInt(equation);
        } else {
            if (equation.contains("+d") || equation.contains("*d")) {
                String separator = equation.contains("+d") ? "+d" : "*d";
                String[] str = equation.split("\\" + separator);
                for (int i = 0; i < str.length; i++) {
                    if (separator.equals("+d")) {
                        endResult += parseEquation(str[i], skillLevel, true);
                    } else if (separator.equals("*d")) {
                        endResult *= parseEquation(str[i], skillLevel, true);
                    }
                }
            } else if (equation.contains("+u") || equation.contains("*u")) {
                String separator = equation.contains("+u") ? "+u" : "*u";
                String[] str = equation.split("\\" + separator);
                for (int i = 0; i < str.length; i++) {
                    if (separator.equals("+u")) {
                        endResult += parseEquation(str[i], skillLevel, true);
                    } else if (separator.equals("*u")) {
                        endResult *= parseEquation(str[i], skillLevel, true);
                    }
                }
            } else {
                endResult = parseEquation(equation, skillLevel, false);
            }
        }
        return endResult;

    }

    private synchronized static int parseEquation(String splitted, int level, boolean implicitMulti) {
        int result = 1;
        lock.lock();
        try {
            if (!variablesSet) {
                synchronized (exp) {
                    exp.addVariable("x", level);
                    exp.addVariable("d", 1);
                    exp.addVariable("u", 1);
                }
                variablesSet = true;
            } else {
                exp.setVarValue("x", level);
                // resetting the levels..
            }
            synchronized (exp) {
                exp.setImplicitMul(true);
                Node n = exp.parse(splitted);
                Object o = exp.evaluate(n);
                String value = String.valueOf(o);
                double d = Double.parseDouble(value);
                result = (int) d;
                if (result < 1) {
                    result = 1;
                }
            }
        } catch (NumberFormatException | ParseException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return result;
    }
}
