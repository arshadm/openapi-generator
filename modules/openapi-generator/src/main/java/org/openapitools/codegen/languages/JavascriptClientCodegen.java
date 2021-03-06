/*
 * Copyright 2018 OpenAPI-Generator Contributors (https://openapi-generator.tech)
 * Copyright 2018 SmartBear Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openapitools.codegen.languages;

import com.google.common.base.Strings;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.codegen.CliOption;
import org.openapitools.codegen.CodegenConfig;
import org.openapitools.codegen.CodegenConstants;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.CodegenType;
import org.openapitools.codegen.DefaultCodegen;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.utils.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class JavascriptClientCodegen extends DefaultCodegen implements CodegenConfig {
    @SuppressWarnings("hiding")
    private static final Logger LOGGER = LoggerFactory.getLogger(JavascriptClientCodegen.class);

    public static final String PROJECT_NAME = "projectName";
    public static final String MODULE_NAME = "moduleName";
    public static final String PROJECT_DESCRIPTION = "projectDescription";
    public static final String PROJECT_VERSION = "projectVersion";
    public static final String USE_PROMISES = "usePromises";
    public static final String USE_INHERITANCE = "useInheritance";
    public static final String EMIT_MODEL_METHODS = "emitModelMethods";
    public static final String EMIT_JS_DOC = "emitJSDoc";
    public static final String USE_ES6 = "useES6";

    final String[][] JAVASCRIPT_SUPPORTING_FILES = new String[][]{
            new String[]{"package.mustache", "package.json"},
            new String[]{"index.mustache", "src/index.js"},
            new String[]{"ApiClient.mustache", "src/ApiClient.js"},
            new String[]{"git_push.sh.mustache", "git_push.sh"},
            new String[]{"README.mustache", "README.md"},
            new String[]{"mocha.opts", "mocha.opts"},
            new String[]{"travis.yml", ".travis.yml"}
    };

    final String[][] JAVASCRIPT_ES6_SUPPORTING_FILES = new String[][]{
            new String[]{"package.mustache", "package.json"},
            new String[]{"index.mustache", "src/index.js"},
            new String[]{"ApiClient.mustache", "src/ApiClient.js"},
            new String[]{"git_push.sh.mustache", "git_push.sh"},
            new String[]{"README.mustache", "README.md"},
            new String[]{"mocha.opts", "mocha.opts"},
            new String[]{"travis.yml", ".travis.yml"},
            new String[]{".babelrc.mustache", ".babelrc"}
    };

    protected String projectName;
    protected String moduleName;
    protected String projectDescription;
    protected String projectVersion;
    protected String licenseName;

    protected String invokerPackage;
    protected String sourceFolder = "src";
    protected String localVariablePrefix = "";
    protected boolean usePromises;
    protected boolean emitModelMethods;
    protected boolean emitJSDoc = true;
    protected String apiDocPath = "docs/";
    protected String modelDocPath = "docs/";
    protected String apiTestPath = "api/";
    protected String modelTestPath = "model/";
    protected boolean useES6 = false; // default is ES5
    private String modelPropertyNaming = "camelCase";

    public JavascriptClientCodegen() {
        super();
        outputFolder = "generated-code/js";
        modelTemplateFiles.put("model.mustache", ".js");
        modelTestTemplateFiles.put("model_test.mustache", ".js");
        apiTemplateFiles.put("api.mustache", ".js");
        apiTestTemplateFiles.put("api_test.mustache", ".js");
        // subfolder Javascript/es6
        embeddedTemplateDir = templateDir = "Javascript" + File.separator + "es6";
        apiPackage = "api";
        modelPackage = "model";
        modelDocTemplateFiles.put("model_doc.mustache", ".md");
        apiDocTemplateFiles.put("api_doc.mustache", ".md");

        // default HIDE_GENERATION_TIMESTAMP to true
        hideGenerationTimestamp = Boolean.TRUE;

        // reference: http://www.w3schools.com/js/js_reserved.asp
        setReservedWordsLowerCase(
                Arrays.asList(
                        "abstract", "arguments", "boolean", "break", "byte",
                        "case", "catch", "char", "class", "const",
                        "continue", "debugger", "default", "delete", "do",
                        "double", "else", "enum", "eval", "export",
                        "extends", "false", "final", "finally", "float",
                        "for", "function", "goto", "if", "implements",
                        "import", "in", "instanceof", "int", "interface",
                        "let", "long", "native", "new", "null",
                        "package", "private", "protected", "public", "return",
                        "short", "static", "super", "switch", "synchronized",
                        "this", "throw", "throws", "transient", "true",
                        "try", "typeof", "var", "void", "volatile",
                        "while", "with", "yield",
                        "Array", "Date", "eval", "function", "hasOwnProperty",
                        "Infinity", "isFinite", "isNaN", "isPrototypeOf",
                        "Math", "NaN", "Number", "Object",
                        "prototype", "String", "toString", "undefined", "valueOf")
        );

        languageSpecificPrimitives = new HashSet<String>(
                Arrays.asList("String", "Boolean", "Number", "Array", "Object", "Date", "File", "Blob")
        );
        defaultIncludes = new HashSet<String>(languageSpecificPrimitives);

        instantiationTypes.put("array", "Array");
        instantiationTypes.put("list", "Array");
        instantiationTypes.put("map", "Object");
        typeMapping.clear();
        typeMapping.put("array", "Array");
        typeMapping.put("map", "Object");
        typeMapping.put("List", "Array");
        typeMapping.put("boolean", "Boolean");
        typeMapping.put("string", "String");
        typeMapping.put("int", "Number");
        typeMapping.put("float", "Number");
        typeMapping.put("number", "Number");
        typeMapping.put("DateTime", "Date");
        typeMapping.put("date", "Date");
        typeMapping.put("long", "Number");
        typeMapping.put("short", "Number");
        typeMapping.put("char", "String");
        typeMapping.put("double", "Number");
        typeMapping.put("object", "Object");
        typeMapping.put("integer", "Number");
        typeMapping.put("ByteArray", "Blob");
        typeMapping.put("binary", "File");
        typeMapping.put("file", "File");
        typeMapping.put("UUID", "String");

        importMapping.clear();

        cliOptions.add(new CliOption(CodegenConstants.SOURCE_FOLDER, CodegenConstants.SOURCE_FOLDER_DESC).defaultValue("src"));
        cliOptions.add(new CliOption(CodegenConstants.LOCAL_VARIABLE_PREFIX, CodegenConstants.LOCAL_VARIABLE_PREFIX_DESC));
        cliOptions.add(new CliOption(CodegenConstants.INVOKER_PACKAGE, CodegenConstants.INVOKER_PACKAGE_DESC));
        cliOptions.add(new CliOption(CodegenConstants.API_PACKAGE, CodegenConstants.API_PACKAGE_DESC));
        cliOptions.add(new CliOption(CodegenConstants.MODEL_PACKAGE, CodegenConstants.MODEL_PACKAGE_DESC));
        cliOptions.add(new CliOption(PROJECT_NAME,
                "name of the project (Default: generated from info.title or \"openapi-js-client\")"));
        cliOptions.add(new CliOption(MODULE_NAME,
                "module name for AMD, Node or globals (Default: generated from <projectName>)"));
        cliOptions.add(new CliOption(PROJECT_DESCRIPTION,
                "description of the project (Default: using info.description or \"Client library of <projectName>\")"));
        cliOptions.add(new CliOption(PROJECT_VERSION,
                "version of the project (Default: using info.version or \"1.0.0\")"));
        cliOptions.add(new CliOption(CodegenConstants.LICENSE_NAME,
                "name of the license the project uses (Default: using info.license.name)"));
        cliOptions.add(new CliOption(USE_PROMISES,
                "use Promises as return values from the client API, instead of superagent callbacks")
                .defaultValue(Boolean.FALSE.toString()));
        cliOptions.add(new CliOption(EMIT_MODEL_METHODS,
                "generate getters and setters for model properties")
                .defaultValue(Boolean.FALSE.toString()));
        cliOptions.add(new CliOption(EMIT_JS_DOC,
                "generate JSDoc comments")
                .defaultValue(Boolean.TRUE.toString()));
        cliOptions.add(new CliOption(USE_INHERITANCE,
                "use JavaScript prototype chains & delegation for inheritance")
                .defaultValue(Boolean.TRUE.toString()));
        cliOptions.add(new CliOption(CodegenConstants.HIDE_GENERATION_TIMESTAMP, CodegenConstants.HIDE_GENERATION_TIMESTAMP_DESC)
                .defaultValue(Boolean.TRUE.toString()));
        cliOptions.add(new CliOption(USE_ES6,
                "use JavaScript ES6 (ECMAScript 6) (beta). Default is ES5.")
                .defaultValue(Boolean.FALSE.toString()));
        cliOptions.add(new CliOption(CodegenConstants.MODEL_PROPERTY_NAMING, CodegenConstants.MODEL_PROPERTY_NAMING_DESC).defaultValue("camelCase"));
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    @Override
    public String getName() {
        return "javascript";
    }

    @Override
    public String getHelp() {
        return "Generates a Javascript client library.";
    }

    @Override
    public void processOpts() {
        if (additionalProperties.containsKey(USE_ES6)) {
            setUseES6(convertPropertyToBooleanAndWriteBack(USE_ES6));
        } else {
            setUseES6(false); // default to ES5
        }
        super.processOpts();

        if (additionalProperties.containsKey(PROJECT_NAME)) {
            setProjectName(((String) additionalProperties.get(PROJECT_NAME)));
        }
        if (additionalProperties.containsKey(MODULE_NAME)) {
            setModuleName(((String) additionalProperties.get(MODULE_NAME)));
        }
        if (additionalProperties.containsKey(PROJECT_DESCRIPTION)) {
            setProjectDescription(((String) additionalProperties.get(PROJECT_DESCRIPTION)));
        }
        if (additionalProperties.containsKey(PROJECT_VERSION)) {
            setProjectVersion(((String) additionalProperties.get(PROJECT_VERSION)));
        }
        if (additionalProperties.containsKey(CodegenConstants.LICENSE_NAME)) {
            setLicenseName(((String) additionalProperties.get(CodegenConstants.LICENSE_NAME)));
        }
        if (additionalProperties.containsKey(CodegenConstants.LOCAL_VARIABLE_PREFIX)) {
            setLocalVariablePrefix((String) additionalProperties.get(CodegenConstants.LOCAL_VARIABLE_PREFIX));
        }
        if (additionalProperties.containsKey(CodegenConstants.SOURCE_FOLDER)) {
            setSourceFolder((String) additionalProperties.get(CodegenConstants.SOURCE_FOLDER));
        }
        if (additionalProperties.containsKey(CodegenConstants.INVOKER_PACKAGE)) {
            setInvokerPackage((String) additionalProperties.get(CodegenConstants.INVOKER_PACKAGE));
        }
        if (additionalProperties.containsKey(USE_PROMISES)) {
            setUsePromises(convertPropertyToBooleanAndWriteBack(USE_PROMISES));
        }
        if (additionalProperties.containsKey(USE_INHERITANCE)) {
            setUseInheritance(convertPropertyToBooleanAndWriteBack(USE_INHERITANCE));
        } else {
            supportsInheritance = true;
            supportsMixins = true;
        }
        if (additionalProperties.containsKey(EMIT_MODEL_METHODS)) {
            setEmitModelMethods(convertPropertyToBooleanAndWriteBack(EMIT_MODEL_METHODS));
        }
        if (additionalProperties.containsKey(EMIT_JS_DOC)) {
            setEmitJSDoc(convertPropertyToBooleanAndWriteBack(EMIT_JS_DOC));
        }
        if (additionalProperties.containsKey(CodegenConstants.MODEL_PROPERTY_NAMING)) {
            setModelPropertyNaming((String) additionalProperties.get(CodegenConstants.MODEL_PROPERTY_NAMING));
        }
    }

    @Override
    public void preprocessOpenAPI(OpenAPI openAPI) {
        super.preprocessOpenAPI(openAPI);

        if (openAPI.getInfo() != null) {
            Info info = openAPI.getInfo();
            if (StringUtils.isBlank(projectName) && info.getTitle() != null) {
                // when projectName is not specified, generate it from info.title
                projectName = sanitizeName(org.openapitools.codegen.utils.StringUtils.dashize(info.getTitle()));
            }
            if (StringUtils.isBlank(projectVersion)) {
                // when projectVersion is not specified, use info.version
                projectVersion = escapeUnsafeCharacters(escapeQuotationMark(info.getVersion()));
            }
            if (projectDescription == null) {
                // when projectDescription is not specified, use info.description
                projectDescription = sanitizeName(info.getDescription());
            }

            // when licenceName is not specified, use info.license
            if (additionalProperties.get(CodegenConstants.LICENSE_NAME) == null && info.getLicense() != null) {
                License license = info.getLicense();
                licenseName = license.getName();
            }
        }

        // default values
        if (StringUtils.isBlank(projectName)) {
            projectName = "openapi-js-client";
        }
        if (StringUtils.isBlank(moduleName)) {
            moduleName = org.openapitools.codegen.utils.StringUtils.camelize(org.openapitools.codegen.utils.StringUtils.underscore(projectName));
        }
        if (StringUtils.isBlank(projectVersion)) {
            projectVersion = "1.0.0";
        }
        if (projectDescription == null) {
            projectDescription = "Client library of " + projectName;
        }
        if (StringUtils.isBlank(licenseName)) {
            licenseName = "Unlicense";
        }

        additionalProperties.put(PROJECT_NAME, projectName);
        additionalProperties.put(MODULE_NAME, moduleName);
        additionalProperties.put(PROJECT_DESCRIPTION, escapeText(projectDescription));
        additionalProperties.put(PROJECT_VERSION, projectVersion);
        additionalProperties.put(CodegenConstants.LICENSE_NAME, licenseName);
        additionalProperties.put(CodegenConstants.API_PACKAGE, apiPackage);
        additionalProperties.put(CodegenConstants.INVOKER_PACKAGE, invokerPackage);
        additionalProperties.put(CodegenConstants.LOCAL_VARIABLE_PREFIX, localVariablePrefix);
        additionalProperties.put(CodegenConstants.MODEL_PACKAGE, modelPackage);
        additionalProperties.put(CodegenConstants.SOURCE_FOLDER, sourceFolder);
        additionalProperties.put(USE_PROMISES, usePromises);
        additionalProperties.put(USE_INHERITANCE, supportsInheritance);
        additionalProperties.put(EMIT_MODEL_METHODS, emitModelMethods);
        additionalProperties.put(EMIT_JS_DOC, emitJSDoc);
        additionalProperties.put(USE_ES6, useES6);

        // make api and model doc path available in mustache template
        additionalProperties.put("apiDocPath", apiDocPath);
        additionalProperties.put("modelDocPath", modelDocPath);

        String[][] supportingTemplateFiles = JAVASCRIPT_SUPPORTING_FILES;
        if (useES6) {
            supportingTemplateFiles = JAVASCRIPT_ES6_SUPPORTING_FILES;
        }

        for (String[] supportingTemplateFile : supportingTemplateFiles) {
            supportingFiles.add(new SupportingFile(supportingTemplateFile[0], "", supportingTemplateFile[1]));
        }
    }

    @Override
    public String escapeReservedWord(String name) {
        if (this.reservedWordsMappings().containsKey(name)) {
            return this.reservedWordsMappings().get(name);
        }
        return "_" + name;
    }

    /**
     * Concatenates an array of path segments into a path string.
     *
     * @param segments The path segments to concatenate. A segment may contain either of the file separator characters '\' or '/'.
     *                 A segment is ignored if it is <code>null</code>, empty or &quot;.&quot;.
     * @return A path string using the correct platform-specific file separator character.
     */
    private String createPath(String... segments) {
        StringBuilder buf = new StringBuilder();
        for (String segment : segments) {
            if (!StringUtils.isEmpty(segment) && !segment.equals(".")) {
                if (buf.length() != 0)
                    buf.append(File.separatorChar);
                buf.append(segment);
            }
        }
        for (int i = 0; i < buf.length(); i++) {
            char c = buf.charAt(i);
            if ((c == '/' || c == '\\') && c != File.separatorChar)
                buf.setCharAt(i, File.separatorChar);
        }
        return buf.toString();
    }

    @Override
    public String apiTestFileFolder() {
        return (outputFolder + "/test/" + apiTestPath).replace('/', File.separatorChar);
    }

    @Override
    public String modelTestFileFolder() {
        return (outputFolder + "/test/" + modelTestPath).replace('/', File.separatorChar);
    }

    @Override
    public String apiFileFolder() {
        return createPath(outputFolder, sourceFolder, invokerPackage, apiPackage());
    }

    @Override
    public String modelFileFolder() {
        return createPath(outputFolder, sourceFolder, invokerPackage, modelPackage());
    }

    public String getInvokerPackage() {
        return invokerPackage;
    }

    public void setInvokerPackage(String invokerPackage) {
        this.invokerPackage = invokerPackage;
    }

    public void setSourceFolder(String sourceFolder) {
        this.sourceFolder = sourceFolder;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setLocalVariablePrefix(String localVariablePrefix) {
        this.localVariablePrefix = localVariablePrefix;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public void setUsePromises(boolean usePromises) {
        this.usePromises = usePromises;
    }

    public void setUseES6(boolean useES6) {
        this.useES6 = useES6;
        if (useES6) {
            embeddedTemplateDir = templateDir = "Javascript/es6";
            LOGGER.info("Using JS ES6 templates");
        } else {
            embeddedTemplateDir = templateDir = "Javascript";
            LOGGER.info("Using JS ES5 templates");
        }
    }

    public void setUseInheritance(boolean useInheritance) {
        this.supportsInheritance = useInheritance;
        this.supportsMixins = useInheritance;
    }

    public void setEmitModelMethods(boolean emitModelMethods) {
        this.emitModelMethods = emitModelMethods;
    }

    public void setEmitJSDoc(boolean emitJSDoc) {
        this.emitJSDoc = emitJSDoc;
    }

    @Override
    public String apiDocFileFolder() {
        return createPath(outputFolder, apiDocPath);
    }

    @Override
    public String modelDocFileFolder() {
        return createPath(outputFolder, modelDocPath);
    }

    @Override
    public String toApiDocFilename(String name) {
        return toApiName(name);
    }

    @Override
    public String toModelDocFilename(String name) {
        return toModelName(name);
    }

    @Override
    public String toApiTestFilename(String name) {
        return toApiName(name) + ".spec";
    }

    @Override
    public String toModelTestFilename(String name) {
        return toModelName(name) + ".spec";
    }

    public String getModelPropertyNaming() {
        return this.modelPropertyNaming;
    }

    private String getNameUsingModelPropertyNaming(String name) {
        switch (CodegenConstants.MODEL_PROPERTY_NAMING_TYPE.valueOf(getModelPropertyNaming())) {
            case original:    return name;
            case camelCase:   return org.openapitools.codegen.utils.StringUtils.camelize(name, true);
            case PascalCase:  return org.openapitools.codegen.utils.StringUtils.camelize(name);
            case snake_case:  return org.openapitools.codegen.utils.StringUtils.underscore(name);
            default:          throw new IllegalArgumentException("Invalid model property naming '" +
                    name + "'. Must be 'original', 'camelCase', " +
                    "'PascalCase' or 'snake_case'");
        }
    }

    @Override
    public String toVarName(String name) {
        // sanitize name
        name = sanitizeName(name);  // FIXME parameter should not be assigned. Also declare it as "final"

        if ("_".equals(name)) {
            name = "_u";
        }

        // if it's all uppper case, do nothing
        if (name.matches("^[A-Z_]*$")) {
            return name;
        }

        // camelize (lower first character) the variable name
        // pet_id => petId
        name = getNameUsingModelPropertyNaming(name);

        // for reserved word or word starting with number, append _
        if (isReservedWord(name) || name.matches("^\\d.*")) {
            name = escapeReservedWord(name);
        }

        return name;
    }

    @Override
    public String toParamName(String name) {
        // should be the same as variable name
        return toVarName(name);
    }

    @Override
    public String toModelName(String name) {
        name = sanitizeName(name);  // FIXME parameter should not be assigned. Also declare it as "final"

        if (!StringUtils.isEmpty(modelNamePrefix)) {
            name = modelNamePrefix + "_" + name;
        }

        if (!StringUtils.isEmpty(modelNameSuffix)) {
            name = name + "_" + modelNameSuffix;
        }

        // camelize the model name
        // phone_number => PhoneNumber
        name = org.openapitools.codegen.utils.StringUtils.camelize(name);

        // model name cannot use reserved keyword, e.g. return
        if (isReservedWord(name)) {
            String modelName = "Model" + name;
            LOGGER.warn(name + " (reserved word) cannot be used as model name. Renamed to " + modelName);
            return modelName;
        }

        // model name starts with number
        if (name.matches("^\\d.*")) {
            String modelName = "Model" + name; // e.g. 200Response => Model200Response (after camelize)
            LOGGER.warn(name + " (model name starts with number) cannot be used as model name. Renamed to " + modelName);
            return modelName;
        }

        return name;
    }

    @Override
    public String toModelFilename(String name) {
        // should be the same as the model name
        return toModelName(name);
    }

    @Override
    public String toModelImport(String name) {
        return name;
    }

    @Override
    public String toApiImport(String name) {
        return toApiName(name);
    }

    @Override
    public String getTypeDeclaration(Schema p) {
        if (ModelUtils.isArraySchema(p)) {
            ArraySchema ap = (ArraySchema) p;
            Schema inner = ap.getItems();
            return "[" + getTypeDeclaration(inner) + "]";
        } else if (ModelUtils.isMapSchema(p)) {
            Schema inner = ModelUtils.getAdditionalProperties(p);
            return "{String: " + getTypeDeclaration(inner) + "}";
        }
        return super.getTypeDeclaration(p);
    }

    @Override
    public String toDefaultValue(Schema p) {
        if (ModelUtils.isBooleanSchema(p)) {
            if (p.getDefault() != null) {
                return p.getDefault().toString();
            }
        } else if (ModelUtils.isDateSchema(p)) {
            // TODO
        } else if (ModelUtils.isDateTimeSchema(p)) {
            // TODO
        } else if (ModelUtils.isNumberSchema(p)) {
            if (p.getDefault() != null) {
                return p.getDefault().toString();
            }
        } else if (ModelUtils.isIntegerSchema(p)) {
            if (p.getDefault() != null) {
                return p.getDefault().toString();
            }
        } else if (ModelUtils.isStringSchema(p)) {
            if (p.getDefault() != null) {
                return "'" + p.getDefault() + "'";
            }
        }

        return null;
    }

    public void setModelPropertyNaming(String naming) {
        if ("original".equals(naming) || "camelCase".equals(naming) ||
                "PascalCase".equals(naming) || "snake_case".equals(naming)) {
            this.modelPropertyNaming = naming;
        } else {
            throw new IllegalArgumentException("Invalid model property naming '" +
                    naming + "'. Must be 'original', 'camelCase', " +
                    "'PascalCase' or 'snake_case'");
        }
    }

    @Override
    public String toDefaultValueWithParam(String name, Schema p) {
        String type = normalizeType(getTypeDeclaration(p));
        if (!StringUtils.isEmpty(p.get$ref())) {
            return " = " + type + ".constructFromObject(data['" + name + "']);";
        } else {
            return " = ApiClient.convertToType(data['" + name + "'], " + type + ");";
        }
    }

    @Override
    public void setParameterExampleValue(CodegenParameter p) {
        String example;

        if (p.defaultValue == null) {
            example = p.example;
        } else {
            example = p.defaultValue;
        }

        String type = p.baseType;
        if (type == null) {
            type = p.dataType;
        }

        if (Boolean.TRUE.equals(p.isInteger)) {
            if (example == null) {
                example = "56";
            }
        } else if (Boolean.TRUE.equals(p.isLong)) {
            if (example == null) {
                example = "789";
            }
        } else if (Boolean.TRUE.equals(p.isDouble)
                || Boolean.TRUE.equals(p.isFloat)
                || Boolean.TRUE.equals(p.isNumber)) {
            if (example == null) {
                example = "3.4";
            }
        } else if (Boolean.TRUE.equals(p.isBoolean)) {
            if (example == null) {
                example = "true";
            }
        } else if (Boolean.TRUE.equals(p.isFile) || Boolean.TRUE.equals(p.isBinary)) {
            if (example == null) {
                example = "/path/to/file";
            }
            example = "\"" + escapeText(example) + "\"";
        } else if (Boolean.TRUE.equals(p.isDate)) {
            if (example == null) {
                example = "2013-10-20";
            }
            example = "new Date(\"" + escapeText(example) + "\")";
        } else if (Boolean.TRUE.equals(p.isDateTime)) {
            if (example == null) {
                example = "2013-10-20T19:20:30+01:00";
            }
            example = "new Date(\"" + escapeText(example) + "\")";
        } else if (Boolean.TRUE.equals(p.isString)) {
            if (example == null) {
                example = p.paramName + "_example";
            }
            example = "\"" + escapeText(example) + "\"";

        } else if (!languageSpecificPrimitives.contains(type)) {
            // type is a model class, e.g. User
            example = "new " + moduleName + "." + type + "()";
        }

        // container
        if (Boolean.TRUE.equals(p.isListContainer)) {
            example = setPropertyExampleValue(p.items);
            example = "[" + example + "]";
        } else if (Boolean.TRUE.equals(p.isMapContainer)) {
            example = setPropertyExampleValue(p.items);
            example = "{key: " + example + "}";
        } else if (example == null) {
            example = "null";
        }

        p.example = example;
    }

    protected String setPropertyExampleValue(CodegenProperty p) {
        String example;

        if (p == null) {
            return "null";
        }

        if (p.defaultValue == null) {
            example = p.example;
        } else {
            example = p.defaultValue;
        }

        String type = p.baseType;
        if (type == null) {
            type = p.dataType;
        }

        if (Boolean.TRUE.equals(p.isInteger)) {
            if (example == null) {
                example = "56";
            }
        } else if (Boolean.TRUE.equals(p.isLong)) {
            if (example == null) {
                example = "789";
            }
        } else if (Boolean.TRUE.equals(p.isDouble)
                || Boolean.TRUE.equals(p.isFloat)
                || Boolean.TRUE.equals(p.isNumber)) {
            if (example == null) {
                example = "3.4";
            }
        } else if (Boolean.TRUE.equals(p.isBoolean)) {
            if (example == null) {
                example = "true";
            }
        } else if (Boolean.TRUE.equals(p.isFile) || Boolean.TRUE.equals(p.isBinary)) {
            if (example == null) {
                example = "/path/to/file";
            }
            example = "\"" + escapeText(example) + "\"";
        } else if (Boolean.TRUE.equals(p.isDate)) {
            if (example == null) {
                example = "2013-10-20";
            }
            example = "new Date(\"" + escapeText(example) + "\")";
        } else if (Boolean.TRUE.equals(p.isDateTime)) {
            if (example == null) {
                example = "2013-10-20T19:20:30+01:00";
            }
            example = "new Date(\"" + escapeText(example) + "\")";
        } else if (Boolean.TRUE.equals(p.isString)) {
            if (example == null) {
                example = p.name + "_example";
            }
            example = "\"" + escapeText(example) + "\"";

        } else if (!languageSpecificPrimitives.contains(type)) {
            // type is a model class, e.g. User
            example = "new " + moduleName + "." + type + "()";
        }

        return example;
    }

    /**
     * Normalize type by wrapping primitive types with single quotes.
     *
     * @param type Primitive type
     * @return Normalized type
     */
    public String normalizeType(String type) {
        return type.replaceAll("\\b(Boolean|Integer|Number|String|Date|Blob)\\b", "'$1'");
    }

    @Override
    public String getSchemaType(Schema p) {
        String openAPIType = super.getSchemaType(p);
        String type = null;
        if (typeMapping.containsKey(openAPIType)) {
            type = typeMapping.get(openAPIType);
            if (!needToImport(type)) {
                return type;
            }
        } else {
            type = openAPIType;
        }
        if (null == type) {
            LOGGER.error("No Type defined for Schema " + p);
        }
        return toModelName(type);
    }

    @Override
    public String toOperationId(String operationId) {
        // throw exception if method name is empty
        if (StringUtils.isEmpty(operationId)) {
            throw new RuntimeException("Empty method/operation name (operationId) not allowed");
        }

        operationId = org.openapitools.codegen.utils.StringUtils.camelize(sanitizeName(operationId), true);

        // method name cannot use reserved keyword, e.g. return
        if (isReservedWord(operationId)) {
            String newOperationId = org.openapitools.codegen.utils.StringUtils.camelize("call_" + operationId, true);
            LOGGER.warn(operationId + " (reserved word) cannot be used as method name. Renamed to " + newOperationId);
            return newOperationId;
        }

        // operationId starts with a number
        if (operationId.matches("^\\d.*")) {
            String newOperationId = org.openapitools.codegen.utils.StringUtils.camelize("call_" + operationId, true);
            LOGGER.warn(operationId + " (starting with a number) cannot be used as method name. Renamed to " + newOperationId);
            return newOperationId;
        }

        return operationId;
    }

    @Override
    public CodegenModel fromModel(String name, Schema model, Map<String, Schema> allDefinitions) {
        CodegenModel codegenModel = super.fromModel(name, model, allDefinitions);

        if (allDefinitions != null && codegenModel != null && codegenModel.parent != null && codegenModel.hasEnums) {
            final Schema parentModel = allDefinitions.get(codegenModel.parentSchema);
            final CodegenModel parentCodegenModel = super.fromModel(codegenModel.parent, parentModel, allDefinitions);
            codegenModel = JavascriptClientCodegen.reconcileInlineEnums(codegenModel, parentCodegenModel);
        }
        if (ModelUtils.isArraySchema(model)) {
            ArraySchema am = (ArraySchema) model;
            if (am.getItems() != null) {
                codegenModel.getVendorExtensions().put("x-isArray", true);
                codegenModel.getVendorExtensions().put("x-itemType", getSchemaType(am.getItems()));
            }
        } else if (ModelUtils.isMapSchema(model)) {
            if (ModelUtils.getAdditionalProperties(model) != null) {
                codegenModel.getVendorExtensions().put("x-isMap", true);
                codegenModel.getVendorExtensions().put("x-itemType", getSchemaType(ModelUtils.getAdditionalProperties(model)));
            } else {
                String type = model.getType();
                if (isPrimitiveType(type)){
                    codegenModel.vendorExtensions.put("x-isPrimitive", true);
                }
            }
        }

        return codegenModel;
    }

    private String sanitizePath(String p) {
        //prefer replace a ', instead of a fuLL URL encode for readability
        return p.replaceAll("'", "%27");
    }

    private String trimBrackets(String s) {
        if (s != null) {
            int beginIdx = s.charAt(0) == '[' ? 1 : 0;
            int endIdx = s.length();
            if (s.charAt(endIdx - 1) == ']')
                endIdx--;
            return s.substring(beginIdx, endIdx);
        }
        return null;
    }

    private String getModelledType(String dataType) {
        return "module:" + (StringUtils.isEmpty(invokerPackage) ? "" : (invokerPackage + "/"))
                + (StringUtils.isEmpty(modelPackage) ? "" : (modelPackage + "/")) + dataType;
    }

    private String getJSDocType(CodegenModel cm, CodegenProperty cp) {
        if (Boolean.TRUE.equals(cp.isContainer)) {
            if (cp.containerType.equals("array"))
                return "Array.<" + getJSDocType(cm, cp.items) + ">";
            else if (cp.containerType.equals("map"))
                return "Object.<String, " + getJSDocType(cm, cp.items) + ">";
        }
        String dataType = trimBrackets(cp.datatypeWithEnum);
        if (cp.isEnum) {
            dataType = cm.classname + '.' + dataType;
        }
        if (isModelledType(cp))
            dataType = getModelledType(dataType);
        return dataType;
    }

    private boolean isModelledType(CodegenProperty cp) {
        // N.B. enums count as modelled types, file is not modelled (SuperAgent uses some 3rd party library).
        return cp.isEnum || !languageSpecificPrimitives.contains(cp.baseType == null ? cp.dataType : cp.baseType);
    }

    private String getJSDocType(CodegenParameter cp) {
        String dataType = trimBrackets(cp.dataType);
        if (isModelledType(cp))
            dataType = getModelledType(dataType);
        if (Boolean.TRUE.equals(cp.isListContainer)) {
            return "Array.<" + dataType + ">";
        } else if (Boolean.TRUE.equals(cp.isMapContainer)) {
            return "Object.<String, " + dataType + ">";
        }
        return dataType;
    }

    private boolean isModelledType(CodegenParameter cp) {
        // N.B. enums count as modelled types, file is not modelled (SuperAgent uses some 3rd party library).
        return cp.isEnum || !languageSpecificPrimitives.contains(cp.baseType == null ? cp.dataType : cp.baseType);
    }

    private String getJSDocType(CodegenOperation co) {
        String returnType = trimBrackets(co.returnType);
        if (returnType != null) {
            if (isModelledType(co))
                returnType = getModelledType(returnType);
            if (Boolean.TRUE.equals(co.isListContainer)) {
                return "Array.<" + returnType + ">";
            } else if (Boolean.TRUE.equals(co.isMapContainer)) {
                return "Object.<String, " + returnType + ">";
            }
        }
        return returnType;
    }

    private boolean isModelledType(CodegenOperation co) {
        // This seems to be the only way to tell whether an operation return type is modelled.
        return !Boolean.TRUE.equals(co.returnTypeIsPrimitive);
    }

    private boolean isPrimitiveType(String type) {
        final String[] primitives = {"number", "integer", "string", "boolean", "null"};
        return Arrays.asList(primitives).contains(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> postProcessOperationsWithModels(Map<String, Object> objs, List<Object> allModels) {
        // Generate and store argument list string of each operation into
        // vendor-extension: x-codegen-argList.
        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        if (operations != null) {
            List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
            for (CodegenOperation operation : ops) {
                List<String> argList = new ArrayList<String>();
                boolean hasOptionalParams = false;
                for (CodegenParameter p : operation.allParams) {
                    if (p.required) {
                        argList.add(p.paramName);
                    } else {
                        hasOptionalParams = true;
                    }
                }
                if (hasOptionalParams) {
                    argList.add("opts");
                }
                if (!usePromises) {
                    argList.add("callback");
                }
                operation.vendorExtensions.put("x-codegen-argList", StringUtils.join(argList, ", "));

                // Store JSDoc type specification into vendor-extension: x-jsdoc-type.
                for (CodegenParameter cp : operation.allParams) {
                    String jsdocType = getJSDocType(cp);
                    cp.vendorExtensions.put("x-jsdoc-type", jsdocType);
                }
                String jsdocType = getJSDocType(operation);
                operation.vendorExtensions.put("x-jsdoc-type", jsdocType);

                // Format the return type correctly
                if (operation.returnType != null) {
                    operation.vendorExtensions.put("x-return-type", normalizeType(operation.returnType));
                }
            }
        }
        return objs;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> postProcessModels(Map<String, Object> objs) {
        objs = super.postProcessModelsEnum(objs);
        List<Object> models = (List<Object>) objs.get("models");
        for (Object _mo : models) {
            Map<String, Object> mo = (Map<String, Object>) _mo;
            CodegenModel cm = (CodegenModel) mo.get("model");

            // Collect each model's required property names in *document order*.
            // NOTE: can't use 'mandatory' as it is built from ModelImpl.getRequired(), which sorts names
            // alphabetically and in any case the document order of 'required' and 'properties' can differ.
            List<CodegenProperty> required = new ArrayList<>();
            List<CodegenProperty> allRequired = supportsInheritance || supportsMixins ? new ArrayList<CodegenProperty>() : required;
            cm.vendorExtensions.put("x-required", required);
            cm.vendorExtensions.put("x-all-required", allRequired);

            for (CodegenProperty var : cm.vars) {
                // Add JSDoc @type value for this property.
                String jsDocType = getJSDocType(cm, var);
                var.vendorExtensions.put("x-jsdoc-type", jsDocType);

                if (Boolean.TRUE.equals(var.required)) {
                    required.add(var);
                }
            }

            if (supportsInheritance || supportsMixins) {
                for (CodegenProperty var : cm.allVars) {
                    if (Boolean.TRUE.equals(var.required)) {
                        allRequired.add(var);
                    }
                }
            }

            // set vendor-extension: x-codegen-hasMoreRequired
            CodegenProperty lastRequired = null;
            for (CodegenProperty var : cm.vars) {
                if (var.required) {
                    lastRequired = var;
                }
            }
            for (CodegenProperty var : cm.vars) {
                if (var == lastRequired) {
                    var.vendorExtensions.put("x-codegen-hasMoreRequired", false);
                } else if (var.required) {
                    var.vendorExtensions.put("x-codegen-hasMoreRequired", true);
                }
            }
        }
        return objs;
    }

    @Override
    protected boolean needToImport(String type) {
        return !defaultIncludes.contains(type)
                && !languageSpecificPrimitives.contains(type);
    }

    private static CodegenModel reconcileInlineEnums(CodegenModel codegenModel, CodegenModel parentCodegenModel) {
        // This generator uses inline classes to define enums, which breaks when
        // dealing with models that have subTypes. To clean this up, we will analyze
        // the parent and child models, look for enums that match, and remove
        // them from the child models and leave them in the parent.
        // Because the child models extend the parents, the enums will be available via the parent.

        // Only bother with reconciliation if the parent model has enums.
        if (parentCodegenModel.hasEnums) {

            // Get the properties for the parent and child models
            final List<CodegenProperty> parentModelCodegenProperties = parentCodegenModel.vars;
            List<CodegenProperty> codegenProperties = codegenModel.vars;

            // Iterate over all of the parent model properties
            boolean removedChildEnum = false;
            for (CodegenProperty parentModelCodegenPropery : parentModelCodegenProperties) {
                // Look for enums
                if (parentModelCodegenPropery.isEnum) {
                    // Now that we have found an enum in the parent class,
                    // and search the child class for the same enum.
                    Iterator<CodegenProperty> iterator = codegenProperties.iterator();
                    while (iterator.hasNext()) {
                        CodegenProperty codegenProperty = iterator.next();
                        if (codegenProperty.isEnum && codegenProperty.equals(parentModelCodegenPropery)) {
                            // We found an enum in the child class that is
                            // a duplicate of the one in the parent, so remove it.
                            iterator.remove();
                            removedChildEnum = true;
                        }
                    }
                }
            }

            if (removedChildEnum) {
                // If we removed an entry from this model's vars, we need to ensure hasMore is updated
                int count = 0, numVars = codegenProperties.size();
                for (CodegenProperty codegenProperty : codegenProperties) {
                    count += 1;
                    codegenProperty.hasMore = (count < numVars) ? true : false;
                }
                codegenModel.vars = codegenProperties;
            }
        }

        return codegenModel;
    }

    private static String sanitizePackageName(String packageName) { // FIXME parameter should not be assigned. Also declare it as "final"
        packageName = packageName.trim();
        packageName = packageName.replaceAll("[^a-zA-Z0-9_\\.]", "_");
        if (Strings.isNullOrEmpty(packageName)) {
            return "invalidPackageName";
        }
        return packageName;
    }

    @Override
    public String toEnumName(CodegenProperty property) {
        return sanitizeName(org.openapitools.codegen.utils.StringUtils.camelize(property.name)) + "Enum";
    }

    @Override
    public String toEnumVarName(String value, String datatype) {
        if (value.length() == 0) {
            return "empty";
        }

        // for symbol, e.g. $, #
        if (getSymbolName(value) != null) {
            return (getSymbolName(value)).toUpperCase(Locale.ROOT);
        }

        return value;
    }

    @Override
    public String toEnumValue(String value, String datatype) {
        if ("Integer".equals(datatype) || "Number".equals(datatype)) {
            return value;
        } else {
            return "\"" + escapeText(value) + "\"";
        }
    }


    @Override
    public String escapeQuotationMark(String input) {
        // remove ', " to avoid code injection
        return input.replace("\"", "").replace("'", "");
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        return input.replace("*/", "*_/").replace("/*", "/_*");
    }

}
