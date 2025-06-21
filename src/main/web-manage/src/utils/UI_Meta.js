const mobile = ref(false);
const touch = ref(false);
const colorScheme = ref("light");
const htmlHtmlElement = document.querySelector("html");
const checkColorScheme = () => {
    if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
        if (htmlHtmlElement.classList.contains("light")) {
            htmlHtmlElement.classList.remove("light");
        }
        htmlHtmlElement.classList.add("dark");
        colorScheme.value = "dark";
    } else {
        if (htmlHtmlElement.classList.contains("dark")) {
            htmlHtmlElement.classList.remove("dark");
        }
        htmlHtmlElement.classList.add("light");
        // htmlHtmlElement.className = "light";
        colorScheme.value = "light";
    }
};
const checkSize = () => {
    mobile.value = window.innerWidth <= 720;
    if (mobile.value) {
        if (!htmlHtmlElement.classList.contains("mobile")) {
            htmlHtmlElement.classList.add("mobile");
        }
    } else {
        htmlHtmlElement.classList.remove("mobile");
    }
};
const checkTouch = () => {
    touch.value = navigator.maxTouchPoints > 0 || navigator.msMaxTouchPoints > 0;
    if (touch.value) {
        htmlHtmlElement.classList.add("touch");
    }
}
checkColorScheme();
checkSize();
checkTouch();
window.addEventListener("resize",checkSize);
window.matchMedia('(prefers-color-scheme: dark)').addEventListener("change",checkColorScheme);
export default {
    mobile:mobile,
    colorScheme: colorScheme,
    touch:touch,
};