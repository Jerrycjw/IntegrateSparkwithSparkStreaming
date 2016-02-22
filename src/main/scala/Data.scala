package IntegrateSpark
import java.io.Serializable
@SerialVersionUID(100L)
class Data (var _type: String, var time: String, var data: String) extends Serializable{
	
	override def toString(): String = "\ntype["+_type+"]\n"+"Data: "+data+"\nMetaData"+"time["+time+"]"
}
