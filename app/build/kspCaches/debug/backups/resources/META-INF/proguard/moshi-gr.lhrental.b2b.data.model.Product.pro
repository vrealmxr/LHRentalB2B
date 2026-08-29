-if class gr.lhrental.b2b.data.model.Product
-keepnames class gr.lhrental.b2b.data.model.Product
-if class gr.lhrental.b2b.data.model.Product
-keep class gr.lhrental.b2b.data.model.ProductJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class gr.lhrental.b2b.data.model.Product
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class gr.lhrental.b2b.data.model.Product
-keepclassmembers class gr.lhrental.b2b.data.model.Product {
    public synthetic <init>(int,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,int,java.lang.String,double,boolean,boolean,boolean,boolean,int,boolean,java.lang.Double,java.lang.Integer,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
