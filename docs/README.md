# The C2MON documentation

The C2MON documentation is written in [Kramdown](https://kramdown.gettalong.org/) and built using [Jekyll](https://jekyllrb.com/) and the [Github-Pages plugin](https://pages.github.com/)

# Preview

To build and view the documentation you will need Jekyll, so make sure to set it up following [this guide](https://jekyllrb.com/docs/installation/). Once you're done, simply open a terminal in this folder and type:

```bash
bundle exec jekyll s --baseurl "" serve --watch
```

The site will be served at [http://127.0.0.1:4000/](http://127.0.0.1:4000/).

# Quick rundown

This project uses:

* Jekyll, a Ruby Gem (library) and [its own Liquid](https://jekyllrb.com/docs/liquid/), extending [Shopify Liquid](https://shopify.github.io/liquid/))
* Github Pages plugin (https://jekyllrb.com/docs/github-pages/)
* Kramdown, a Ruby based parser for a superset of [Markdown](https://daringfireball.net/projects/markdown/)
* [Rouge](http://rouge.jneen.net/) a Ruby code highligher
* Folder structure (see [Jekyll's guide](https://jekyllrb.com/docs/structure/))
  * \_data: Anything you want to access as static, formatted site data (mainly .ymls and .jsons).
  * \_includes: HTML files to be included as part of other HTML files. Simple, right?
  * \_posts: Normally you have all your content here but this project mainly uses static pages mapped (see [#Navigation])
  * assets: Jekyll tosses these files straight to the output (\_site) without parsing them, so put your images, javascript, etc, here
  * docs: This is where we have most of the site's conte

While this is a standard stack for this type of blog, almost all these tools extend other well known and popular tools, so be careful as to the the commands and syntax used

# Theme

The theme currently used is the open source [alkoclick/geneva-noire](https://github.com/alkoclick/geneva-noire), an open source Jekyll theme adapted for this project. You can edit and change themes in the \_config.yml file. If you wish to change it, see [this guide](https://help.github.com/en/articles/adding-a-jekyll-theme-to-your-github-pages-site)

# Navigation

Jekyll offers no built in support for nav menus, so we have to build our own. This is done in the \_includes/nav.html file, which loads content based on the \_data/menu.yml file. Read in the instructions in that file for syntax and how to add additional pages

# Common issues

_My Github Pages blog doesn't build due to error XYZ?_
Have a look [here](https://help.github.com/en/articles/troubleshooting-github-pages-builds)

_Symlinks don't work and cause build errors!_
If you make them work, good job! Make a PR to us

_Plugin XYZ doesn't work!_
Github-pages plugin is only compatible with [a few selected plugins](https://help.github.com/en/articles/configuring-jekyll-plugins)

_None of the above :(_
The \_site folder contains the generated files, you can troubleshoot most issues by checking those
