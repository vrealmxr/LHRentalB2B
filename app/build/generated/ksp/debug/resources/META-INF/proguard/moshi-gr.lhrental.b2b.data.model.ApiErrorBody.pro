-if class gr.lhrental.b2b.data.model.ApiErrorBody
-keepnames class gr.lhrental.b2b.data.model.ApiErrorBody
-if class gr.lhrental.b2b.data.model.ApiErrorBody
-keep class gr.lhrental.b2b.data.model.ApiErrorBodyJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class gr.lhrental.b2b.data.model.ApiErrorBody
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class gr.lhrental.b2b.data.model.ApiErrorBody
-keepclassmembers class gr.lhrental.b2b.data.model.ApiErrorBody {
    public synthetic <init>(boolean,gr.lhrental.b2b.data.model.ApiError,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
