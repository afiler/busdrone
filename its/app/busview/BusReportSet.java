package its.app.busview;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Vector;

public class BusReportSet
  implements Externalizable
{
  public long timestamp;
  public Vector array;
  static final long serialVersionUID = 4476752156644304612L;

  public BusReportSet()
  {
  }

  public void writeExternal(ObjectOutput o)
    throws IOException
  {
    o.writeLong(this.timestamp);
    if (this.array == null)
    {
      o.writeObject(null);
    }
    else
    {
      BusReport[] arrayOfBusReport = new BusReport[this.array.size()];
      this.array.copyInto(arrayOfBusReport);
      o.writeObject(arrayOfBusReport);
    }
  }

  public void readExternal(ObjectInput o)
    throws IOException
  {
	this.timestamp = o.readLong();
    try
    {
      BusReport[] busReports = (BusReport[])o.readObject();
      if (busReports != null)
      {
        this.array = new Vector(busReports.length);
        for (int i = 0; i < busReports.length; i++)
          this.array.addElement(busReports[i]);
      }
    }
    catch (ClassNotFoundException localClassNotFoundException)
    {
    }
  }

  public void a(BusReport paramBusReport)
  {
    this.array.addElement(paramBusReport);
  }

  public Vector array()
  {
    return this.array;
  }
}
