-if class gr.lhrental.b2b.data.model.Category
-keepnames class gr.lhrental.b2b.data.model.Category
-if class gr.lhrental.b2b.data.model.Category
-keep class gr.lhrental.b2b.data.model.CategoryJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class gr.lhrental.b2b.data.model.Category
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class gr.lhrental.b2b.data.model.Category
-keepclassmembers class gr.lhrental.b2b.data.model.Category {
    public synthetic <init>(int,java.lang.String,java.lang.String,java.lang.String,boolean,java.util.List,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
