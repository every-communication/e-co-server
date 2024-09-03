package com.eco.ecoserver.global.config;

import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("eco-server")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public Info apiInfo() {
        return new Info().title("e-co Swagger")
                .description("e-co Swagger")
                .version("1.0");
    }
    /* @Bean
     public Docket api() {
         return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo())
         .select()
         .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
         .build();
     }*/
    /*@Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }


    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("e-co Swagger")
                .description("e-co Swagger")
                .version("1.0")
                .build();
    }*/
}