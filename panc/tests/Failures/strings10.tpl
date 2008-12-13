#
# base64 encoded strings
#

object template strings10;

"/s1" = "[" + base64_decode("aGVsbG8gd29ybGQ=") + "]";
"/s2" = base64_decode(<<EOT);
  H4sIAOwLyDwAA02PQQ7DMAgE731FX9BT1f8QZ52iYhthEiW/r2SitCdmxCK0E3W8no+36n2G
  8UbOrYYWGROCgurBe4JeCexI2ahgWF5rulaLtImkDxbucS0tcc3t5GXMAqeZnIYo+TvAmsL8
  GGLobbUUX7pT+pxkXJc/5Bx5p0ki7Cgq5KccGrCR8PzruUfP2xfJgVqHCgEAAA==
EOT
