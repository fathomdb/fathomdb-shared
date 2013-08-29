//package com.fathomdb.jersey;
//
//import java.io.IOException;
//import java.io.OutputStream;
//
//import javax.ws.rs.ext.Provider;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.fathomdb.handlebars.HandlebarsTemplating;
//import com.fathomdb.viewable.Viewable;
//
//@Provider
//public class HandlebarsViewRenderer implements ViewProcessor<String> {
//
//    private static final Logger log = LoggerFactory.getLogger(HandlebarsViewProcessor.class);
//
//    final HandlebarsTemplating handlebars;
//
//    public HandlebarsViewProcessor() {
//        this.handlebars = HandlebarsTemplating.get();
//    }
//
//    @Override
//    public String resolve(String path) {
//        return handlebars.resolve(path);
//    }
//
//    @Override
//    public void writeTo(final String resolvedPath, Viewable viewable, OutputStream out) throws IOException {
//        // Commit the status and headers to the HttpServletResponse
//        out.flush();
//
//        Object model = viewable.getModel();
//
//        handlebars.writeTo(resolvedPath, model, out);
//    }
//
// }