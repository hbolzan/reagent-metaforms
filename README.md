# Reagent Metaforms

This is a Reagent + Re-frame based project. It's main goal is to provide a tool to help build CRUD forms without coding

## Motivation
Many former desktop developers (me included) have a hard time trying to learn how to write frontend web applications. What if there was a framework that needed no coding and they could focus only on backend business logic? That's what I'm trying to achieve.


## Reagent with shadow-cljs

This is a minimal setup for using Reagent with shadow-cljs. It supports
production builds and live-reloading.

## Getting started

Run `yarn` to download dependencies.

To have a local development server running do `yarn dev`; then visit
[localhost:8020](http://localhost:8020). You can configure the port in
`shadow-cljs.edn`. Whenever you make a change it will automatically reload the
code and render again.

To make a production build use `yarn release`.

To configure further please check out
[shadow-cljs](https://github.com/thheller/shadow-cljs).
