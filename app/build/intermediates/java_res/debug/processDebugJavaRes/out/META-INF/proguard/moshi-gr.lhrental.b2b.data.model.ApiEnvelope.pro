-if class gr.lhrental.b2b.data.model.ApiEnvelope
-keepnames class gr.lhrental.b2b.data.model.ApiEnvelope
-if class gr.lhrental.b2b.data.model.ApiEnvelope
-keep class gr.lhrental.b2b.data.model.ApiEnvelopeJsonAdapter {
    public <init>(com.squareup.moshi.Moshi,java.lang.reflect.Type[]);
}
-if class gr.lhrental.b2b.data.model.ApiEnvelope
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class gr.lhrental.b2b.data.model.ApiEnvelope
-keepclassmembers class gr.lhrental.b2b.data.model.ApiEnvelope {
    public synthetic <init>(boolean,java.lang.Object,gr.lhrental.b2b.data.model.ApiError,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
