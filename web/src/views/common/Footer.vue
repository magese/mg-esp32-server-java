<template>
  <div class="footer">
    <div class="footer-copyright">
      Copyright
      <a-icon type="copyright" />
      {{ new Date().getFullYear()}} By Magese. All rights reserved
    </div>
  </div>
</template>

<style lang="scss" scoped>
.footer {
   text-align: center;
}
.footer a, .footer-copyright, .footer-beian a {
  color: rgba(0,0,0,.45);
  transition: all 0.3s;
}
</style>
