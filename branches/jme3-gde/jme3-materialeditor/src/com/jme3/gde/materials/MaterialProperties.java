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
    FileObject material;
    FileObject matDef;
    Map<String, MaterialProperty> values = new HashMap<String, MaterialProperty>();
//    List<String> matDefNames = new LinkedList<String>();
    ProjectAssetManager manager;
    FileSystem fs;
    public static final String[] variableTypes = new String[]{"Int", "Boolean", "Float", "Vector2", "Vector3", "Vector4", "Color", "Texture2D", "TextureCubeMap"};

    public MaterialProperties(FileObject material, ProjectAssetManager manager) {
        this.material = material;
        this.manager = manager;
    }

    public void read() {
        values.clear();
//        matDefNames.clear();
        int level = 0;
        boolean params = false;
        try {
            //material
            for (String line : material.asLines()) {
                line = line.trim();
                if (line.startsWith("Material ") || line.startsWith("Material\t") && level == 0) {
                    readMaterialProperties(line);
                }
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
                    int colonIdx = line.indexOf(":");
                    if (colonIdx != -1) {
                        String[] lines = line.split(":");
                        MaterialProperty prop = new MaterialProperty();
                        prop.setName(lines[0].trim());
                        if (lines.length > 1) {
                            prop.setValue(lines[1].trim());
                        }
                        values.put(prop.getName(), prop);
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
                                String name = line.replaceFirst(string, "").trim();
                                MaterialProperty prop = values.get(name);
                                if (prop == null) {
                                    prop = new MaterialProperty();
                                    prop.setName(name);
                                    prop.setValue("");
                                    values.put(prop.getName(), prop);
                                }
                                prop.setType(string);
//                                matDefNames.add(prop.getName());
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void readMaterialProperties(String line) {
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

    public String getUpdatedContent() {
        boolean params = false;
        int level = 0;
        try {
            List<String> matLines = material.asLines();
            StringWriter out = new StringWriter();
            List<String> setValues = new LinkedList<String>();
            for (String line : matLines) {
                String newLine = line;
                line = line.trim();
                if (line.startsWith("Material ") || line.startsWith("Material\t") && level == 0) {
                    String suffix = "";
                    if (line.indexOf("{") > -1) {
                        suffix = "{";
                    }
                    newLine = "Material " + getName() + " : " + matDefName +" "+ suffix;
                    //readMaterialProperties(line);
                }
                if (line.startsWith("MaterialParameters ") || line.startsWith("MaterialParameters\t") || line.startsWith("MaterialParameters{") && level == 1) {
                    params = true;
                }
                if (line.indexOf("{") != -1) {
                    level++;
                }
                if (line.indexOf("}") != -1) {
                    level--;
                    if (params) {
                        //find and write parameters we did not replace yet at end of parameters section
                        for (Iterator<Map.Entry<String, MaterialProperty>> it = values.entrySet().iterator(); it.hasNext();) {
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
                }
                if (level == 2 && params) {
                    int colonIdx = newLine.indexOf(":");
                    if (colonIdx != -1) {
                        String[] lines = newLine.split(":");
                        String myName = lines[0].trim();
                        if (values.containsKey(myName)) {
                            setValues.add(myName);
                            MaterialProperty prop = values.get(myName);
                            if (prop.getValue() != null && prop.getValue().length() > 0 && prop.getType()!=null) {
                                newLine = lines[0] + ": " + prop.getValue();
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

    public Map<String, MaterialProperty> getMap() {
        return values;
    }

    public void setValue(String key, MaterialProperty value) {
        values.put(key, value);
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
