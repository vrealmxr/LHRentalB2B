-if class gr.lhrental.b2b.data.model.User
-keepnames class gr.lhrental.b2b.data.model.User
-if class gr.lhrental.b2b.data.model.User
-keep class gr.lhrental.b2b.data.model.UserJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class gr.lhrental.b2b.data.model.User
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class gr.lhrental.b2b.data.model.User
-keepclassmembers class gr.lhrental.b2b.data.model.User {
    public synthetic <init>(int,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
