# ngDesk-Angular Coding Guide

## Coding Guide vocabulary
Each guideline describes either a good or bad practice, and all have a consistent presentation.

The wording of each guideline indicates how strong the recommendation is.

**Do** is one that should always be followed. Always might be a bit too strong of a word. Guidelines that literally should always be followed are extremely rare. On the other hand, you need a really unusual case for breaking a Do guideline.

**Consider** guidelines should generally be followed. If you fully understand the meaning behind the guideline and have a good reason to deviate, then do so. Please strive to be consistent.

**Avoid** indicates something you should almost never do. Code examples to avoid have an unmistakable red header.

**Why?** gives reasons for following the previous recommendations.

**Example**

## Text

### Classes
#### Guide 01-01


**Do** Add one of the following classes to every text element. Classes: .mat-display-4, .mat-display-3, .mat-display-2, .mat-display-1, .mat-h1, .mat-h2, .mat-h3, .mat-h4, .mat-body, .mat-body-2, .mat-small

**Why** This will keep the same font sizes and styles throughout the application

## Services

### API Calls
#### Guide 02-01

**Do** Make the name of a function in a service the type of API request (get, put, post, delete) followed by the name of the object

**Why?** This keep the files consistent

**Example** getEntries(), getEntry(), postEntry(), putSchedule, deleteSchedule()


## Routing

### Path Parameters
#### Guide 03-01

**Do** Make the name of path parameters camelCase in a route

**Why?** This keeps the routes consistent

**Example** `{ path: 'path/:firstPathParameter/detail/:secondPathParameter', component: SomeComponent }`

#### Guide 03-02

**Do** Make the of path kebab-case in a route

**Why?** This keeps the routes consistent

**Example** `{ path: 'this-is/some-fixed-path', component: SomeComponent }`


## HTML

### Spacing
#### Guide 04-01

**Do** Make spacing between elements on the screen be in increments of 10px

**Why?** This keeps the look consistent


### Form error message
#### Guide 04-02

**Do** Include the name of the field in the message and make the issue with it **bold**

**Why?** This clearly tells and highlights what is wrong to the user

**Why?** This keeps the error messages consistent

### Clickable elements
#### Guide 04-03

**Do** All clickable elements **must** have the pointer mouse when they are hovered over

**Why?** This signals to the user that this element is clickable
