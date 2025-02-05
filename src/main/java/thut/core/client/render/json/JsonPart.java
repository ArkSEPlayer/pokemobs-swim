package thut.core.client.render.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.math.Quaternion;

import net.minecraft.core.Direction;
import thut.api.maths.Vector4;
import thut.core.client.render.json.JsonTemplate.JsonBlock;
import thut.core.client.render.json.JsonTemplate.JsonFace;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.model.parts.Mesh;
import thut.core.client.render.model.parts.Part;
import thut.core.client.render.texturing.TextureCoordinate;

public class JsonPart extends Part
{

    public static List<JsonPart> makeParts(JsonBlock b, int index)
    {
        final List<JsonPart> parts = new ArrayList<>();
        float[] offsets;

        if (b.rotation == null) offsets = new float[]
        { 0, 0, 0 };
        else offsets = b.rotation.origin.clone();

        List<Mesh> shapes = makeShapes(b, offsets);
        JsonPart root = make(shapes.get(0), b.name, b, index);
        parts.add(root);
        if (shapes.size() > 1)
        {
            int i = 0;
            for (Mesh s : shapes)
            {
                JsonPart part = make(s, b.name + "_" + i++, b, index);
                parts.add(part);
            }
        }
        return parts;
    }

    private static JsonPart make(Mesh mesh, String name, JsonBlock b, int index)
    {
        JsonPart part = new JsonPart(name);
        part.index = index;
        part.addShape(mesh);
        float[] offsets = b.from.clone();
        offsets = new float[]{0,0,0};
        if (b.rotation != null)
        {
            offsets[0] += b.rotation.origin[0];
            offsets[1] += b.rotation.origin[1];
            offsets[2] += b.rotation.origin[2];
            
            float x = 0;
            float y = 0;
            float z = 0;
            if (b.rotation.axis.equals("x"))
            {
                x = b.rotation.angle;
            }
            if (b.rotation.axis.equals("y"))
            {
                y = b.rotation.angle;
            }
            if (b.rotation.axis.equals("z"))
            {
                z = b.rotation.angle;
            }
            Quaternion quat = new Quaternion(x, y, z, true);
            final Vector4 rotations = new Vector4(quat);
            part.rotations.set(rotations.x, rotations.y, rotations.z, rotations.w);
        }
        offsets[0]/=16;
        offsets[1]/=16;
        offsets[2]/=16;
        part.offset.set(offsets);
        return part;
    }

    private static void addFace(Map<String, List<List<Object>>> materials, JsonBlock b, JsonFace face, Direction dir,
            float[] offsets)
    {
        List<Object> order = Lists.newArrayList();
        List<Object> verts = Lists.newArrayList();
        List<Object> tex = Lists.newArrayList();
        String material = face.texture;
        if (materials.containsKey(material))
        {
            List<List<Object>> lists = materials.get(material);
            order = lists.get(0);
            verts = lists.get(1);
            tex = lists.get(2);
        }
        else
        {
            materials.put(material, Lists.newArrayList(order, verts, tex));
        }

        float[] from =
        { 0, 0, 0 };
        float[] to =
        { b.to[0] - b.from[0], b.to[1] - b.from[1], b.to[2] - b.from[2] };

        from = b.from.clone();
        to = b.to.clone();


        from[0] -= offsets[0];
        from[1] -= offsets[1];
        from[2] -= offsets[2];

        to[0] -= offsets[0];
        to[1] -= offsets[1];
        to[2] -= offsets[2];

        int[][] tex_order =
        {
                { 0, 3 },
                { 0, 2 },
                { 1, 2 },
                { 1, 3 } };

        float[][] coords = new float[4][3];

        switch (dir)
        {
        case DOWN:
            coords[3][0] = to[0];
            coords[3][2] = from[2];

            coords[2][0] = to[0];
            coords[2][2] = to[2];

            coords[1][0] = from[0];
            coords[1][2] = to[2];

            coords[0][0] = from[0];
            coords[0][2] = from[2];

            coords[0][1] = to[1];
            coords[1][1] = to[1];
            coords[2][1] = to[1];
            coords[3][1] = to[1];
            break;
        case EAST:
            coords[0][1] = from[1];
            coords[0][0] = from[0];

            coords[1][1] = from[1];
            coords[1][0] = to[0];

            coords[2][1] = to[1];
            coords[2][0] = to[0];

            coords[3][1] = to[1];
            coords[3][0] = from[0];

            coords[0][2] = to[2];
            coords[1][2] = to[2];
            coords[2][2] = to[2];
            coords[3][2] = to[2];
            break;
        case NORTH:
            coords[0][1] = to[1];
            coords[0][2] = from[2];

            coords[1][1] = to[1];
            coords[1][2] = to[2];

            coords[2][1] = from[1];
            coords[2][2] = to[2];

            coords[3][1] = from[1];
            coords[3][2] = from[2];

            coords[0][0] = to[0];
            coords[1][0] = to[0];
            coords[2][0] = to[0];
            coords[3][0] = to[0];
            break;
        case SOUTH:
            coords[0][1] = from[1];
            coords[0][2] = from[2];

            coords[1][1] = from[1];
            coords[1][2] = to[2];

            coords[2][1] = to[1];
            coords[2][2] = to[2];

            coords[3][1] = to[1];
            coords[3][2] = from[2];

            coords[0][0] = from[0];
            coords[1][0] = from[0];
            coords[2][0] = from[0];
            coords[3][0] = from[0];
            break;
        case UP:
            coords[0][0] = to[0];
            coords[0][2] = from[2];

            coords[1][0] = to[0];
            coords[1][2] = to[2];

            coords[2][0] = from[0];
            coords[2][2] = to[2];

            coords[3][0] = from[0];
            coords[3][2] = from[2];

            coords[0][1] = from[1];
            coords[1][1] = from[1];
            coords[2][1] = from[1];
            coords[3][1] = from[1];
            break;
        case WEST:
            coords[0][1] = from[1];
            coords[0][0] = from[0];

            coords[1][1] = from[1];
            coords[1][0] = to[0];

            coords[2][1] = to[1];
            coords[2][0] = to[0];

            coords[3][1] = to[1];
            coords[3][0] = from[0];

            coords[0][2] = from[2];
            coords[1][2] = from[2];
            coords[2][2] = from[2];
            coords[3][2] = from[2];
            break;
        default:
            break;
        }

        for (int i = 0; i < 4; i++)
        {
            float[] c = coords[i];
            Vertex v = new Vertex(c[0] / 16f, c[1] / 16f, c[2] / 16f);
            Integer o = order.size();
            int u0 = tex_order[i][0];
            int v0 = tex_order[i][1];
            TextureCoordinate t = new TextureCoordinate(face.uv[u0] / 16f, face.uv[v0] / 16f);
            order.add(o);
            verts.add(v);
            tex.add(t);
        }

        for (int i = 3; i >= 0; i--)
        {
            float[] c = coords[i];
            Vertex v = new Vertex(c[0] / 16f, c[1] / 16f, c[2] / 16f);
            Integer o = order.size();
            int u0 = tex_order[i][0];
            int v0 = tex_order[i][1];
            TextureCoordinate t = new TextureCoordinate(face.uv[u0] / 16f, face.uv[v0] / 16f);
            order.add(o);
            verts.add(v);
            tex.add(t);
        }
    }

    private static List<Mesh> makeShapes(JsonBlock b, float[] offsets)
    {
        List<Mesh> shapes = new ArrayList<>();

        Map<String, List<List<Object>>> materials = Maps.newHashMap();

        // The vertices and order come from drawing the 6 quads, based on
        // the from and to coordinates of the block

        // The normals can be ignored, as they will be calculated by the mesh
        // and are assumed to be flat shaded.

        // The texture coordinates from from the faces,
        // if they have different coordinates, it makes a new mesh for that face

        addFace(materials, b, b.faces.north, Direction.NORTH, offsets);
        addFace(materials, b, b.faces.east, Direction.EAST, offsets);
        addFace(materials, b, b.faces.south, Direction.SOUTH, offsets);
        addFace(materials, b, b.faces.west, Direction.WEST, offsets);
        addFace(materials, b, b.faces.up, Direction.UP, offsets);
        addFace(materials, b, b.faces.down, Direction.DOWN, offsets);

        materials.forEach((key, lists) -> {
            List<Object> order = lists.get(0);
            List<Object> verts = lists.get(1);
            List<Object> tex = lists.get(2);
            Mesh m = new JsonMesh(order.toArray(new Integer[0]), verts.toArray(new Vertex[0]),
                    tex.toArray(new TextureCoordinate[0]));
            shapes.add(m);
        });
        return shapes;
    }

    public int index = 0;

    public JsonPart(String name)
    {
        super(name + "");
    }

    @Override
    public String getType()
    {
        return "json";
    }
}
