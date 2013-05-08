package its.app.busview;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class BusReport
  implements Externalizable
{
  public long timestamp;
  public long brField1;
  public short coach;
  public short route;
  public int timepoint;
  public int dest;
  public int x;
  public int y;
  public short direction;
  static final long serialVersionUID = -7892157182165587445L;
  
  public void writeExternal(ObjectOutput paramObjectOutput)
    throws IOException
  {
    paramObjectOutput.writeLong(this.timestamp);
    paramObjectOutput.writeShort(this.coach);
    paramObjectOutput.writeShort(this.route);
    paramObjectOutput.writeLong(this.brField1);
    paramObjectOutput.writeInt(this.timepoint);
    paramObjectOutput.writeInt(this.dest);
    paramObjectOutput.writeInt(this.x);
    paramObjectOutput.writeInt(this.y);
    paramObjectOutput.writeShort(this.direction);
  }

  public void readExternal(ObjectInput paramObjectInput)
    throws IOException
  {
    this.timestamp = paramObjectInput.readLong();
    this.coach = paramObjectInput.readShort();
    this.route = paramObjectInput.readShort();
    this.brField1 = paramObjectInput.readLong();
    this.timepoint = paramObjectInput.readInt();
    this.dest = paramObjectInput.readInt();
    this.x = paramObjectInput.readInt();
    this.y = paramObjectInput.readInt();
    this.direction = paramObjectInput.readShort();
  }
}
