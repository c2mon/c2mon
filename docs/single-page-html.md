---
layout: post
---

{% assign totalContent = site.posts | concat: site.pages | where_exp:"post","post.title" | where_exp: "post", "post.url contains 'docs'" %}

{%- include dfs.html tree=site.data.menu base='/' content=totalContent -%}
