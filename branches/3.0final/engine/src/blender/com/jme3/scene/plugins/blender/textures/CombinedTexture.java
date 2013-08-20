package com.jme3.scene.plugins.blender.textures;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import jme3tools.converters.ImageToAwt;

import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.plugins.blender.BlenderContext;
import com.jme3.scene.plugins.blender.BlenderContext.LoadedFeatureDataType;
import com.jme3.scene.plugins.blender.file.Structure;
import com.jme3.scene.plugins.blender.materials.MaterialContext;
import com.jme3.scene.plugins.blender.textures.TriangulatedTexture.TriangleTextureElement;
import com.jme3.scene.plugins.blender.textures.UVCoordinatesGenerator.UVCoordinatesType;
import com.jme3.scene.plugins.blender.textures.UVProjectionGenerator.UVProjectionType;
import com.jme3.scene.plugins.blender.textures.blending.TextureBlender;
import com.jme3.scene.plugins.blender.textures.blending.TextureBlenderFactory;
import com.jme3.scene.plugins.blender.textures.io.PixelIOFactory;
import com.jme3.scene.plugins.blender.textures.io.PixelInputOutput;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.Texture2D;

/**
 * This class represents a texture that is defined for the material. It can be
 * made of several textures (both 2D and 3D) that are merged together and
 * returned as a single texture.
 * 
 * @author Marcin Roguski (Kaelthas)
 */
public class CombinedTexture {
    private static final Logger LOGGER       = Logger.getLogger(CombinedTexture.class.getName());

    /** The mapping type of the texture. Defined bu MaterialContext.MTEX_COL, MTEX_NOR etc. */
    private final int           mappingType;
    /** The data for each of the textures. */
    private List<TextureData>   textureDatas = new ArrayList<TextureData>();
    /** The result texture. */
    private Texture             resultTexture;
    /** The UV values for the result texture. */
    private List<Vector2f>      resultUVS;

    /**
     * Constructor. Stores the texture mapping type (ie. color map, normal map).
     * 
     * @param mappingType
     *            texture mapping type
     */
    public CombinedTexture(int mappingType) {
        this.mappingType = mappingType;
    }

    /**
     * This method adds a texture data to the resulting texture.
     * 
     * @param texture
     *            the source texture
     * @param textureBlender
     *            the texture blender (to mix the texture with its material
     *            color)
     * @param uvCoordinatesType
     *            the type of UV coordinates
     * @param projectionType
     *            the type of UV coordinates projection (for flat textures)
     * @param textureStructure
     *            the texture sructure
     * @param uvCoordinatesName
     * 			  the name of the used user's UV coordinates for this texture
     * @param blenderContext
     *            the blender context
     */
    public void add(Texture texture, TextureBlender textureBlender, int uvCoordinatesType, int projectionType, Structure textureStructure, String uvCoordinatesName, BlenderContext blenderContext) {
        if (!(texture instanceof GeneratedTexture) && !(texture instanceof Texture2D)) {
            throw new IllegalArgumentException("Unsupported texture type: " + (texture == null ? "null" : texture.getClass()));
        }
        if (!(texture instanceof GeneratedTexture) || blenderContext.getBlenderKey().isLoadGeneratedTextures()) {
            if (UVCoordinatesGenerator.isTextureCoordinateTypeSupported(UVCoordinatesType.valueOf(uvCoordinatesType))) {
                TextureData textureData = new TextureData();
                textureData.texture = texture;
                textureData.textureBlender = textureBlender;
                textureData.uvCoordinatesType = UVCoordinatesType.valueOf(uvCoordinatesType);
                textureData.projectionType = UVProjectionType.valueOf(projectionType);
                textureData.textureStructure = textureStructure;
                textureData.uvCoordinatesName = uvCoordinatesName;

                if (textureDatas.size() > 0 && this.isWithoutAlpha(textureData, blenderContext)) {
                    textureDatas.clear();// clear previous textures, they will be covered anyway
                }
                textureDatas.add(textureData);
            } else {
                LOGGER.warning("The texture coordinates type is not supported: " + UVCoordinatesType.valueOf(uvCoordinatesType) + ". The texture '" + textureStructure.getName() + "'.");
            }
        }
    }

    /**
     * This method flattens the texture and creates a single result of Texture2D
     * type.
     * 
     * @param geometry
     *            the geometry the texture is created for
     * @param geometriesOMA
     *            the old memory address of the geometries list that the given
     *            geometry belongs to (needed for bounding box creation)
     * @param userDefinedUVCoordinates
     *            the UV's defined by user (null or zero length table if none
     *            were defined)
     * @param blenderContext
     *            the blender context
     */
    @SuppressWarnings("unchecked")
    public void flatten(Geometry geometry, Long geometriesOMA, LinkedHashMap<String, List<Vector2f>> userDefinedUVCoordinates, BlenderContext blenderContext) {
        TextureHelper textureHelper = blenderContext.getHelper(TextureHelper.class);
        Mesh mesh = geometry.getMesh();
        Texture previousTexture = null;
        UVCoordinatesType masterUVCoordinatesType = null;
        String masterUserUVSetName = null;
        for (TextureData textureData : textureDatas) {
            // decompress compressed textures (all will be merged into one texture anyway)
            if (textureDatas.size() > 1 && textureData.texture.getImage().getFormat().isCompressed()) {
                textureData.texture.setImage(textureHelper.decompress(textureData.texture.getImage()));
                textureData.textureBlender = TextureBlenderFactory.alterTextureType(textureData.texture.getImage().getFormat(), textureData.textureBlender);
            }

            if (previousTexture == null) {// the first texture will lead the others to its shape
                if (textureData.texture instanceof GeneratedTexture) {
                    resultTexture = ((GeneratedTexture) textureData.texture).triangulate(mesh, geometriesOMA, textureData.uvCoordinatesType, blenderContext);
                } else if (textureData.texture instanceof Texture2D) {
                    resultTexture = textureData.texture;

                    if (textureData.uvCoordinatesType == UVCoordinatesType.TEXCO_UV && userDefinedUVCoordinates != null && userDefinedUVCoordinates.size() > 0) {
                        if(textureData.uvCoordinatesName == null) {
                            resultUVS = userDefinedUVCoordinates.values().iterator().next();//get the first UV available
                        } else {
                            resultUVS = userDefinedUVCoordinates.get(textureData.uvCoordinatesName);
                        }
                        masterUserUVSetName = textureData.uvCoordinatesName;
                    } else {
                        List<Geometry> geometries = (List<Geometry>) blenderContext.getLoadedFeature(geometriesOMA, LoadedFeatureDataType.LOADED_FEATURE);
                        resultUVS = UVCoordinatesGenerator.generateUVCoordinatesFor2DTexture(mesh, textureData.uvCoordinatesType, textureData.projectionType, geometries);
                    }
                }
                this.blend(resultTexture, textureData.textureBlender, blenderContext);

                previousTexture = resultTexture;
                masterUVCoordinatesType = textureData.uvCoordinatesType;
            } else {
                if (textureData.texture instanceof GeneratedTexture) {
                    if (!(resultTexture instanceof TriangulatedTexture)) {
                        resultTexture = new TriangulatedTexture((Texture2D) resultTexture, resultUVS, blenderContext);
                        resultUVS = null;
                        previousTexture = resultTexture;
                    }

                    TriangulatedTexture triangulatedTexture = ((GeneratedTexture) textureData.texture).triangulate(mesh, geometriesOMA, textureData.uvCoordinatesType, blenderContext);
                    triangulatedTexture.castToUVS((TriangulatedTexture) resultTexture, blenderContext);
                    triangulatedTexture.blend(textureData.textureBlender, (TriangulatedTexture) resultTexture, blenderContext);
                    resultTexture = previousTexture = triangulatedTexture;
                } else if (textureData.texture instanceof Texture2D) {
                    if (this.isUVTypesMatch(masterUVCoordinatesType, masterUserUVSetName,
                                             textureData.uvCoordinatesType, textureData.uvCoordinatesName) &&
                        resultTexture instanceof Texture2D) {
                        this.scale((Texture2D) textureData.texture, resultTexture.getImage().getWidth(), resultTexture.getImage().getHeight());
                        this.merge((Texture2D) resultTexture, (Texture2D) textureData.texture);
                        previousTexture = resultTexture;
                    } else {
                        if (!(resultTexture instanceof TriangulatedTexture)) {
                            resultTexture = new TriangulatedTexture((Texture2D) resultTexture, resultUVS, blenderContext);
                            resultUVS = null;
                        }
                        // first triangulate the current texture
                        List<Vector2f> textureUVS = null;
                        if (textureData.uvCoordinatesType == UVCoordinatesType.TEXCO_UV && userDefinedUVCoordinates != null && userDefinedUVCoordinates.size() > 0) {
                            if(textureData.uvCoordinatesName == null) {
                                textureUVS = userDefinedUVCoordinates.values().iterator().next();//get the first UV available
                            } else {
                                textureUVS = userDefinedUVCoordinates.get(textureData.uvCoordinatesName);
                            }
                        } else {
                            List<Geometry> geometries = (List<Geometry>) blenderContext.getLoadedFeature(geometriesOMA, LoadedFeatureDataType.LOADED_FEATURE);
                            textureUVS = UVCoordinatesGenerator.generateUVCoordinatesFor2DTexture(mesh, textureData.uvCoordinatesType, textureData.projectionType, geometries);
                        }
                        TriangulatedTexture triangulatedTexture = new TriangulatedTexture((Texture2D) textureData.texture, textureUVS, blenderContext);
                        // then move the texture to different UV's
                        triangulatedTexture.castToUVS((TriangulatedTexture) resultTexture, blenderContext);
                        ((TriangulatedTexture) resultTexture).merge(triangulatedTexture);
                    }
                }
            }
        }

        if (resultTexture instanceof TriangulatedTexture) {
            if (mappingType == MaterialContext.MTEX_NOR) {
                for (int i = 0; i < ((TriangulatedTexture) resultTexture).getFaceTextureCount(); ++i) {
                    TriangleTextureElement triangleTextureElement = ((TriangulatedTexture) resultTexture).getFaceTextureElement(i);
                    triangleTextureElement.image = textureHelper.convertToNormalMapTexture(triangleTextureElement.image, 1);// TODO: get proper strength factor
                }
            }
            resultUVS = ((TriangulatedTexture) resultTexture).getResultUVS();
            resultTexture = ((TriangulatedTexture) resultTexture).getResultTexture();
        }

        // setting additional data
        resultTexture.setWrap(WrapMode.Repeat);
        // the filters are required if generated textures are used because
        // otherwise ugly lines appear between the mesh faces
        resultTexture.setMagFilter(MagFilter.Nearest);
        resultTexture.setMinFilter(MinFilter.NearestNoMipMaps);
    }

    /**
     * The method checks if the texture UV coordinates match.
     * It the types are equal and different then UVCoordinatesType.TEXCO_UV then we consider them a match.
     * If they are both UVCoordinatesType.TEXCO_UV then they match only when their UV sets names are equal.
     * In other cases they are considered NOT a match.
     * @param type1 the UV coord type
     * @param uvSetName1 the user's UV coords set name (considered only for UVCoordinatesType.TEXCO_UV)
     * @param type2 the UV coord type
     * @param uvSetName2 the user's UV coords set name (considered only for UVCoordinatesType.TEXCO_UV)
     * @return <b>true</b> if the types match and <b>false</b> otherwise
     */
    private boolean isUVTypesMatch(UVCoordinatesType type1, String uvSetName1,
                                     UVCoordinatesType type2, String uvSetName2) {
        if(type1 == type2) {
            if(type1 == UVCoordinatesType.TEXCO_UV) {
                if(uvSetName1 != null && uvSetName2 != null && uvSetName1.equals(uvSetName2)) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }
    
    /**
     * This method blends the texture.
     * 
     * @param texture
     *            the texture to be blended
     * @param textureBlender
     *            blending definition for the texture
     * @param blenderContext
     *            the blender context
     */
    private void blend(Texture texture, TextureBlender textureBlender, BlenderContext blenderContext) {
        if (texture instanceof TriangulatedTexture) {
            ((TriangulatedTexture) texture).blend(textureBlender, null, blenderContext);
        } else if (texture instanceof Texture2D) {
            Image blendedImage = textureBlender.blend(texture.getImage(), null, blenderContext);
            texture.setImage(blendedImage);
        } else {
            throw new IllegalArgumentException("Invalid type for texture to blend!");
        }
    }

    /**
     * @return the result texture
     */
    public Texture getResultTexture() {
        return resultTexture;
    }

    /**
     * @return the result UV coordinates
     */
    public List<Vector2f> getResultUVS() {
        return resultUVS;
    }

    /**
     * @return the amount of added textures
     */
    public int getTexturesCount() {
        return textureDatas.size();
    }

    /**
     * @return <b>true</b> if the texture has at least one generated texture component and <b>false</b> otherwise
     */
    public boolean hasGeneratedTextures() {
        if (textureDatas != null) {
            for (TextureData textureData : textureDatas) {
                if (textureData.texture instanceof GeneratedTexture) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method merges two given textures. The result is stored in the
     * 'target' texture.
     * 
     * @param target
     *            the target texture
     * @param source
     *            the source texture
     */
    private void merge(Texture2D target, Texture2D source) {
        if (target.getImage().getDepth() != source.getImage().getDepth()) {
            throw new IllegalArgumentException("Cannot merge images with different depths!");
        }
        Image sourceImage = source.getImage();
        Image targetImage = target.getImage();
        PixelInputOutput sourceIO = PixelIOFactory.getPixelIO(sourceImage.getFormat());
        PixelInputOutput targetIO = PixelIOFactory.getPixelIO(targetImage.getFormat());
        TexturePixel sourcePixel = new TexturePixel();
        TexturePixel targetPixel = new TexturePixel();
        int depth = target.getImage().getDepth() == 0 ? 1 : target.getImage().getDepth();

        for (int layerIndex = 0; layerIndex < depth; ++layerIndex) {
            for (int x = 0; x < sourceImage.getWidth(); ++x) {
                for (int y = 0; y < sourceImage.getHeight(); ++y) {
                    sourceIO.read(sourceImage, layerIndex, sourcePixel, x, y);
                    targetIO.read(targetImage, layerIndex, targetPixel, x, y);
                    targetPixel.merge(sourcePixel);
                    targetIO.write(targetImage, layerIndex, targetPixel, x, y);
                }
            }
        }
    }

    /**
     * This method determines if the given texture has no alpha channel.
     * 
     * @param texture
     *            the texture to check for alpha channel
     * @return <b>true</b> if the texture has no alpha channel and <b>false</b>
     *         otherwise
     */
    private boolean isWithoutAlpha(TextureData textureData, BlenderContext blenderContext) {
        ColorBand colorBand = new ColorBand(textureData.textureStructure, blenderContext);
        if (!colorBand.hasTransparencies()) {
            int type = ((Number) textureData.textureStructure.getFieldValue("type")).intValue();
            if (type == TextureHelper.TEX_MAGIC) {
                return true;
            }
            if (type == TextureHelper.TEX_VORONOI) {
                int voronoiColorType = ((Number) textureData.textureStructure.getFieldValue("vn_coltype")).intValue();
                return voronoiColorType != 0;// voronoiColorType == 0:
                                             // intensity, voronoiColorType
                                             // != 0: col1, col2 or col3
            }
            if (type == TextureHelper.TEX_CLOUDS) {
                int sType = ((Number) textureData.textureStructure.getFieldValue("stype")).intValue();
                return sType == 1;// sType==0: without colors, sType==1: with
                                  // colors
            }

            // checking the flat textures for alpha values presence
            if (type == TextureHelper.TEX_IMAGE) {
                Image image = textureData.texture.getImage();
                switch (image.getFormat()) {
                    case BGR8:
                    case DXT1:
                    case Luminance16:
                    case Luminance16F:
                    case Luminance32F:
                    case Luminance8:
                    case RGB10:
                    case RGB111110F:
                    case RGB16:
                    case RGB16F:
                    case RGB32F:
                    case RGB565:
                    case RGB8:
                        return true;// these types have no alpha by definition
                    case ABGR8:
                    case DXT3:
                    case DXT5:
                    case Luminance16Alpha16:
                    case Luminance16FAlpha16F:
                    case Luminance8Alpha8:
                    case RGBA16:
                    case RGBA16F:
                    case RGBA32F:
                    case RGBA8:// with these types it is better to make sure if the texture is or is not transparent
                        PixelInputOutput pixelInputOutput = PixelIOFactory.getPixelIO(image.getFormat());
                        TexturePixel pixel = new TexturePixel();
                        int depth = image.getDepth() == 0 ? 1 : image.getDepth();
                        for (int layerIndex = 0; layerIndex < depth; ++layerIndex) {
                            for (int x = 0; x < image.getWidth(); ++x) {
                                for (int y = 0; y < image.getHeight(); ++y) {
                                    pixelInputOutput.read(image, layerIndex, pixel, x, y);
                                    if (pixel.alpha < 1.0f) {
                                        return false;
                                    }
                                }
                            }
                        }
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * This method scales the given texture to the given size.
     * 
     * @param texture
     *            the texture to be scaled
     * @param width
     *            new width of the texture
     * @param height
     *            new height of the texture
     */
    private void scale(Texture2D texture, int width, int height) {
        // first determine if scaling is required
        boolean scaleRequired = texture.getImage().getWidth() != width || texture.getImage().getHeight() != height;

        if (scaleRequired) {
            Image image = texture.getImage();
            BufferedImage sourceImage = ImageToAwt.convert(image, false, true, 0);

            int sourceWidth = sourceImage.getWidth();
            int sourceHeight = sourceImage.getHeight();

            BufferedImage targetImage = new BufferedImage(width, height, sourceImage.getType());

            Graphics2D g = targetImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(sourceImage, 0, 0, width, height, 0, 0, sourceWidth, sourceHeight, null);
            g.dispose();

            Image output = new ImageLoader().load(targetImage, false);
            image.setWidth(width);
            image.setHeight(height);
            image.setData(output.getData(0));
            image.setFormat(output.getFormat());
        }
    }

    /**
     * A simple class to aggregate the texture data (improves code quality).
     * 
     * @author Marcin Roguski (Kaelthas)
     */
    private static class TextureData {
        /** The texture. */
        public Texture           texture;
        /** The texture blender (to mix the texture with its material color). */
        public TextureBlender    textureBlender;
        /** The type of UV coordinates. */
        public UVCoordinatesType uvCoordinatesType;
        /** The type of UV coordinates projection (for flat textures). */
        public UVProjectionType  projectionType;
        /** The texture sructure. */
        public Structure         textureStructure;
        /** The name of the user's UV coordinates that are used for this texture. */
        public String	  		 uvCoordinatesName;
    }
}
