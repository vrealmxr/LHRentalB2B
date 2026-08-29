-if class gr.lhrental.b2b.data.model.Invoice
-keepnames class gr.lhrental.b2b.data.model.Invoice
-if class gr.lhrental.b2b.data.model.Invoice
-keep class gr.lhrental.b2b.data.model.InvoiceJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
