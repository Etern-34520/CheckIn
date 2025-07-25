const mobile = ref(false);
const touch = ref(false);
const inPWA = ref(false);
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
const checkPWA = () => {
    inPWA.value =
        window.matchMedia('(display-mode: window-controls-overlay)').matches ||
        window.matchMedia('(display-mode: standalone)').matches
}
checkColorScheme();
checkSize();
checkTouch();
checkPWA();
window.addEventListener("resize", checkSize);
window.matchMedia('(prefers-color-scheme: dark)').addEventListener("change", checkColorScheme);
window.matchMedia('(display-mode: window-controls-overlay').addEventListener("change", checkPWA);
window.matchMedia('(display-mode: standalone').addEventListener("change", checkPWA);
export default {
    mobile: mobile,
    colorScheme: colorScheme,
    touch: touch,
    inPWA: inPWA
};