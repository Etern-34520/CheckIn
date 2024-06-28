const mobile = ref(false);
const checkMobile = () => {
    mobile.value = window.innerWidth < 960;
};
checkMobile();
window.addEventListener("resize",checkMobile);
export default {mobile:mobile};