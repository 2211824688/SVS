<template>
  <NavBar />
  <router-view/>
</template>

<script>
  import NavBar from './components/NavBar.vue';
  import 'bootstrap/dist/css/bootstrap.css';
  import 'bootstrap/dist/js/bootstrap';
  // import $ from 'jquery';
  export default {
    name:"APP",
    components: {
      NavBar
    },
    setup() {
    }
  }
</script>

<style>
body {
  background-image: url("@/assets/images/background3.png");
  background-size: cover;
}
</style>