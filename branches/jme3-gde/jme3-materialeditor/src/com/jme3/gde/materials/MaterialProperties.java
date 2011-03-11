/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.materials;

import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.system.JmeSystem;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author normenhansen
 */
public class MaterialProperties {

    private String name;
    private String matDefName;
    private FileObject material;
    private FileObject matDef;
    private Map<String, MaterialProperty> materialParameters = new HashMap<String, MaterialProperty>();
    private Map<String, MaterialProperty> additionalRenderStates = new HashMap<String, MaterialProperty>();
    private ProjectAssetManager manager;
    private FileSystem fs;
    public static final String[] variableTypes = new String[]{"Int", "Boolean", "Float", "Vector2", "Vector3", "Vector4", "Color", "Texture2D", "TextureCubeMap"};

    public MaterialProperties(FileObject material, ProjectAssetManager manager) {
        this.material = material;
        this.manager = manager;
    }

    /**
     * loads the data from the material and matdef files
     */
    public void read() {
        prepareAdditionalStates();
        materialParameters.clear();
        int level = 0;
        boolean params = false;
        boolean states = false;
        try {
            //TODO: make/use parser
            //material
            for (String line : material.asLines()) {
                line = trimLine(line);
                if (line.startsWith("Material ") || line.startsWith("Material\t") && level == 0) {
                    findMatDef(line);
                }
                if (line.startsWith("MaterialParameters ") || line.startsWith("MaterialParameters\t") || line.startsWith("MaterialParameters{") && level == 1) {
                    params = true;
                }
                if (line.startsWith("AdditionalRenderStates ") || line.startsWith("AdditionalRenderStates\t") || line.startsWith("AdditionalRenderStates{") && level == 1) {
                    states = true;
                }
                if (line.indexOf("{") != -1) {
                    level++;
                }
                if (line.indexOf("}") != -1) {
                    level--;
                    if (params) {
                        params = false;
                    }
                    if (states) {
                        states = false;
                    }
                }
                if (level == 2 && params) {
                    int colonIdx = line.indexOf(":");
                    if (colonIdx != -1) {
                        String[] lines = line.split(":");
                        MaterialProperty prop = new MaterialProperty();
                        prop.setName(lines[0].trim());
                        if (lines.length > 1) {
                            prop.setValue(lines[lines.length - 1].trim());
                        }
                        materialParameters.put(prop.getName(), prop);
                    }
                }
                if (level == 2 && states) {
                    String[] lines = null;
                    int colonIdx = line.indexOf(" ");
                    if (colonIdx != -1) {
                        lines = line.split(" ");
                    }
                    colonIdx = line.indexOf("\t");
                    if (colonIdx != -1) {
                        lines = line.split("\t");
                    }
                    if (lines != null) {
                        MaterialProperty prop = new MaterialProperty();
                        String name = lines[0].trim();
                        prop.setName(name);
                        if (additionalRenderStates.get(name) != null) {
                            prop.setType(additionalRenderStates.get(name).getType());
                        }
                        if (lines.length > 1) {
                            prop.setValue(lines[lines.length - 1].trim());
                        }
                        additionalRenderStates.put(prop.getName(), prop);
                    }
                }
            }
            //matdef
            if (matDef != null && matDef.isValid()) {
                for (String line : matDef.asLines()) {
                    line = line.trim();
                    if (line.startsWith("MaterialParameters ") || line.startsWith("MaterialParameters\t") || line.startsWith("MaterialParameters{") && level == 1) {
                        params = true;
                    }
                    if (line.indexOf("{") != -1) {
                        level++;
                    }
                    if (line.indexOf("}") != -1) {
                        level--;
                        if (params) {
                            params = false;
                        }
                    }
                    if (level == 2 && params) {
                        for (int i = 0; i < variableTypes.length; i++) {
                            String string = variableTypes[i];
                            if (line.startsWith(string)) {
                                String name = trimName(line.replaceFirst(string, ""));
                                MaterialProperty prop = materialParameters.get(name);
                                if (prop == null) {
                                    prop = new MaterialProperty();
                                    prop.setName(name);
                                    prop.setValue("");
                                    materialParameters.put(prop.getName(), prop);
                                }
                                prop.setType(string);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * finds the matdef file either from project or from base jme
     * @param line
     */
    private void findMatDef(String line) {
        int colonIdx = line.indexOf(":");
        if (colonIdx != -1) {
            line = line.replaceFirst("Material", "");
            line = line.replace("{", "");
            String[] lines = line.split(":");
            setName(lines[0].trim());
            setMatDefName(lines[1].trim());
            matDef = FileUtil.toFileObject(new File(manager.getFolderName() + "/" + getMatDefName()).getAbsoluteFile());
            //try to read from classpath if not in assets folder
            if (matDef == null || !matDef.isValid()) {
                try {
                    fs = FileUtil.createMemoryFileSystem();
                    matDef = fs.getRoot().createData(name, "j3md");
                    OutputStream out = matDef.getOutputStream();
                    InputStream in = JmeSystem.getResourceAsStream("/" + getMatDefName());
                    if (in != null) {
                        int input = in.read();
                        while (input != -1) {
                            out.write(input);
                            input = in.read();
                        }
                        in.close();
                    }
                    out.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    /**
     * returns the new content of the material file, filled with the new parameters
     * @return
     */
    public String getUpdatedContent() {
        boolean params = false;
        boolean states = false;
        boolean addedstates = false;
        int level = 0;
        try {
            List<String> matLines = material.asLines();
            StringWriter out = new StringWriter();
            List<String> setValues = new LinkedList<String>();
            List<String> setStates = new LinkedList<String>();
            //goes through the lines of the material file and replaces the values it finds
            for (String line : matLines) {
                String newLine = line;
                line = trimLine(line);
                if (line.startsWith("Material ") || line.startsWith("Material\t") && level == 0) {
                    String suffix = "";
                    if (line.indexOf("{") > -1) {
                        suffix = "{";
                    }
                    newLine = "Material " + getName() + " : " + matDefName + " " + suffix;
                }
                if (line.startsWith("MaterialParameters ") || line.startsWith("MaterialParameters\t") || line.startsWith("MaterialParameters{") && level == 1) {
                    params = true;
                }
                if (line.startsWith("AdditionalRenderStates ") || line.startsWith("AdditionalRenderStates\t") || line.startsWith("AdditionalRenderStates{") && level == 1) {
                    states = true;
                    addedstates = true;
                }
                if (line.indexOf("{") != -1) {
                    level++;
                }
                if (line.indexOf("}") != -1) {
                    level--;
                    if (params) {
                        //find and write parameters we did not replace yet at end of parameters section
                        for (Iterator<Map.Entry<String, MaterialProperty>> it = materialParameters.entrySet().iterator(); it.hasNext();) {
                            Map.Entry<String, MaterialProperty> entry = it.next();
                            if (!setValues.contains(entry.getKey())) {
                                MaterialProperty prop = entry.getValue();
                                if (prop.getValue() != null && prop.getValue().length() > 0) {
                                    String myLine = "        " + prop.getName() + " : " + prop.getValue() + "\n";
                                    out.write(myLine, 0, myLine.length());
                                }
                            }
                        }
                        params = false;
                    }
                    if (states) {
                        //find and write states we did not replace yet at end of states section
                        for (Iterator<Map.Entry<String, MaterialProperty>> it = additionalRenderStates.entrySet().iterator(); it.hasNext();) {
                            Map.Entry<String, MaterialProperty> entry = it.next();
                            if (!setStates.contains(entry.getKey())) {
                                MaterialProperty prop = entry.getValue();
                                if (prop.getValue() != null && prop.getValue().length() > 0) {
                                    String myLine = "        " + prop.getName() + " " + prop.getValue() + "\n";
                                    out.write(myLine, 0, myLine.length());
                                }
                            }
                        }
                        states = false;
                    }
                    if (level == 0) {
                        //add renderstates if they havent been in the file yet
                        if (!addedstates) {
                            boolean started = false;
                            for (Iterator<Map.Entry<String, MaterialProperty>> it = additionalRenderStates.entrySet().iterator(); it.hasNext();) {
                                Map.Entry<String, MaterialProperty> entry = it.next();
                                if (!setStates.contains(entry.getKey())) {
                                    MaterialProperty prop = entry.getValue();
                                    if (prop.getValue() != null && prop.getValue().length() > 0) {
                                        if (!started) {
                                            started = true;
                                            String myLine = "    AdditionalRenderStates{\n";
                                            out.write(myLine, 0, myLine.length());
                                        }
                                        String myLine = "        " + prop.getName() + " " + prop.getValue() + "\n";
                                        out.write(myLine, 0, myLine.length());
                                    }
                                }
                            }
                            if (started) {
                                String myLine = "    }\n";
                                out.write(myLine, 0, myLine.length());
                            }
                        }
                    }
                }
                if (level == 2 && params) {
                    int colonIdx = newLine.indexOf(":");
                    if (colonIdx != -1) {
                        String[] lines = newLine.split(":");
                        String myName = lines[0].trim();
                        if (materialParameters.containsKey(myName)) {
                            setValues.add(myName);
                            MaterialProperty prop = materialParameters.get(myName);
                            if (prop.getValue() != null && prop.getValue().length() > 0 && prop.getType() != null) {
                                newLine = lines[0] + ": " + prop.getValue();
                            } else {
                                newLine = null;
                            }
                        }
                    }
                }
                if (level == 2 && states) {
                    String cutLine = newLine.trim();
                    String[] lines = null;
                    int colonIdx = cutLine.indexOf(" ");
                    if (colonIdx != -1) {
                        lines = cutLine.split(" ");
                    }
                    colonIdx = cutLine.indexOf("\t");
                    if (colonIdx != -1) {
                        lines = cutLine.split("\t");
                    }
                    if (lines != null) {
                        String myName = lines[0].trim();
                        if (additionalRenderStates.containsKey(myName)) {
                            setStates.add(myName);
                            MaterialProperty prop = additionalRenderStates.get(myName);
                            if (prop.getValue() != null && prop.getValue().length() > 0 && prop.getType() != null) {
                                newLine = "      " + lines[0] + " " + prop.getValue();
                            } else {
                                newLine = null;
                            }
                        }
                    }
                }
                if (newLine != null) {
                    out.write(newLine + "\n", 0, newLine.length() + 1);
                }
            }
            out.close();
            return out.toString();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return "";
    }

    private void prepareAdditionalStates() {
        additionalRenderStates.clear();
        additionalRenderStates.put("WireFrame", new MaterialProperty("OnOff", "WireFrame", ""));
        additionalRenderStates.put("DepthWrite", new MaterialProperty("OnOff", "DepthWrite", ""));
        additionalRenderStates.put("DepthTest", new MaterialProperty("OnOff", "DepthTest", ""));
        additionalRenderStates.put("ColorWrite", new MaterialProperty("OnOff", "ColorWrite", ""));
        additionalRenderStates.put("PointSprite", new MaterialProperty("OnOff", "PointSprite", ""));
        additionalRenderStates.put("FaceCull", new MaterialProperty("FaceCullMode", "FaceCull", ""));
        additionalRenderStates.put("Blend", new MaterialProperty("BlendMode", "Blend", ""));
        additionalRenderStates.put("AlphaTestFalloff", new MaterialProperty("Float", "AlphaTestFalloff", ""));
        additionalRenderStates.put("PolyOffset", new MaterialProperty("Float,Float", "PolyOffset", ""));
    }

    /**
     * trims a line and removes comments
     * @param line
     * @return
     */
    private String trimLine(String line) {
        int idx = line.indexOf("//");
        if (idx != -1) {
            line = line.substring(0, idx);
        }
        return line.trim();
    }

    /**
     * trims a line and removes everything behind colon
     * @param line
     * @return
     */
    private String trimName(String line) {
        line = trimLine(line);
        int idx = line.indexOf(":");
        if (idx != -1) {
            line = line.substring(0, idx);
        }
        return line.trim();
    }

    public Map<String, MaterialProperty> getParameterMap() {
        return materialParameters;
    }

    public Map<String, MaterialProperty> getStateMap() {
        return additionalRenderStates;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the matDefName
     */
    public String getMatDefName() {
        return matDefName;
    }

    /**
     * @param matDefName the matDefName to set
     */
    public void setMatDefName(String matDefName) {
        this.matDefName = matDefName;
    }

    public String getMaterialPath() {
        return manager.getRelativeAssetPath(material.getPath());
    }
}
