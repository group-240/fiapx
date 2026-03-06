package com.fiap.fiapx.config;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.util.Iterator;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FIAPX API")
                        .version("1.0.0")
                        .description("API do projeto FIAPX - FIAP Tech Challenge")
                        .contact(new Contact()
                                .name("FIAP Team")
                                .email("contato@fiap.com.br"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }

    @Bean
    public ModelConverter multipartFileConverter() {
        return new ModelConverter() {
            @Override
            public Schema resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
                if (type.getType().equals(MultipartFile.class)) {
                    Schema schema = new Schema();
                    schema.setType("string");
                    schema.setFormat("binary");
                    return schema;
                } else if (type.getType().getTypeName().contains("MultipartFile[]")) {
                    Schema arraySchema = new Schema();
                    arraySchema.setType("array");
                    Schema itemSchema = new Schema();
                    itemSchema.setType("string");
                    itemSchema.setFormat("binary");
                    arraySchema.setItems(itemSchema);
                    return arraySchema;
                }
                if (chain.hasNext()) {
                    return chain.next().resolve(type, context, chain);
                } else {
                    return null;
                }
            }
        };
    }
}
