# ngDesk-Angular Style Guide

## Style Guide vocabulary

Each guideline describes either a good or bad practice, and all have a consistent presentation.

The wording of each guideline indicates how strong the recommendation is.

**Do** is one that should always be followed. Always might be a bit too strong of a word. Guidelines that literally should always be followed are extremely rare. On the other hand, you need a really unusual case for breaking a Do guideline.

**Consider** guidelines should generally be followed. If you fully understand the meaning behind the guideline and have a good reason to deviate, then do so. Please strive to be consistent.

**Avoid** indicates something you should almost never do. Code examples to avoid have an unmistakable red header.

**Why?** gives reasons for following the previous recommendations.

## Single responsibility

### Rule of One

#### Style 01-01

**Do** define one thing, such as a service or component, per file.

**Consider** limiting files to 400 lines of code.

**Why?** One component per file makes it far easier to read, maintain, and avoid collisions with teams in source control.

**Why?** One component per file avoids hidden bugs that often arise when combining components in a file where they may share variables, create unwanted closures, or unwanted coupling with dependencies.

**Why?** A single component can be the default export for its file which facilitates lazy loading with the router.

### Small functions

#### Style 01-02

**Do** define small functions

**Consider** limiting to no more than 75 lines.

**Why?** Small functions are easier to test, especially when they do one thing and serve one purpose.

**Why?** Small functions promote reuse.

**Why?** Small functions are easier to read.

**Why?** Small functions are easier to maintain.

**Why?** Small functions help avoid hidden bugs that come with large functions that share variables with external scope, create unwanted closures, or unwanted coupling with dependencies.

## Naming

### General Naming Guidelines

#### Style 02-01

**Do** use consistent names for all symbols.

**Do** follow a pattern that describes the symbol's feature then its type. The recommended pattern is feature.type.ts.

**Why?** Naming conventions help provide a consistent way to find content at a glance. Consistency within the project is vital. Consistency with a team is important. Consistency across a company provides tremendous efficiency.

**Why?** The naming conventions should simply help find desired code faster and make it easier to understand.

**Why?** Names of folders and files should clearly convey their intent. For example, app/heroes/hero-list.component.ts may contain a component that manages a list of heroes.

### Separate file names with dots and dashes

#### Style 02-02

**Do** use dashes to separate words in the descriptive name.

**Do** use dots to separate the descriptive name from the type.

**Do** use consistent type names for all components following a pattern that describes the component's feature then its type. A recommended pattern is feature.type.ts.

**Do** use conventional type names including .service, .component, .pipe, .module, and .directive. Invent additional type names if you must but take care not to create too many.

**Why?** Type names provide a consistent way to quickly identify what is in the file.

**Why?** Type names make it easy to find a specific file type using an editor or IDE's fuzzy search techniques.

**Why?** Unabbreviated type names such as .service are descriptive and unambiguous. Abbreviations such as .srv, .svc, and .serv can be confusing.

**Why?** Type names provide pattern matching for any automated tasks.

## Coding conventions

### Classes

#### Style 03-01

**Do** use PascalCase when naming classes. ThisIsAnExampleOfPascalCase.

**Why?** Follows conventional thinking for class names.

**Why?** Classes can be instantiated and construct an instance. By convention, upper camel case indicates a constructable asset.

### Import line spacing

#### Style 03-02

**Do** leaving one empty line between third party imports and application imports.

**Do** listing import lines alphabetized by the module.

**Do** listing destructured imported symbols alphabetically.

**Why?** The empty line separates your stuff from their stuff.

**Why?** Alphabetizing makes it easier to read and locate symbols.

## HTML

### Attribute and directive ordering

#### Style 04-01

**Do** Use the following order for attributes and directives: material properties, flex properties, angular properties.

**Why?** Using a consistent order provides a consistent way to locale directives and attributes.

## Typescript

### Datatypes

#### Style 05-01

**Do** Use the following types: string, number, ect...

**Avoid** Using type any

**Why?** Typing the variables make the code easier to read and prevents bugs

## Commenting

### Self describing code

#### Style 05-01

**Do** Name functions and variables so that they describe the behavior, so comments are not need. Longer more descriptive names are better than confusing short name.

**Why?** This make the code self describing and removes the need for comments in most places

## Reference

Most of the style in this guide come from https://angular.io/guide/styleguide but there are differences and this guide is the record of truth for this project
