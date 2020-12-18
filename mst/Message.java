package mst;
import java.io.Serializable;

enum Type{
    initiate,test,accept,reject,report,change_root,connect
}
public class Message implements Serializable {
    public Type type;
    public int LN;
    public int FN;
    public int weight;
    public NodeState state;
    public Message(Type type,int fragmentLevel, int fragmentID,NodeState state,int weight){
        this.type = type;
        this.LN = fragmentLevel;
        this.FN = fragmentID;
        this.weight=weight;
        this.state=state;
    }
}

