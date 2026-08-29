-if class gr.lhrental.b2b.data.model.ApiError
-keepnames class gr.lhrental.b2b.data.model.ApiError
-if class gr.lhrental.b2b.data.model.ApiError
-keep class gr.lhrental.b2b.data.model.ApiErrorJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
