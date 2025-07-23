import axios from "axios";
import {ElMessage} from "element-plus";

// axios.defaults.baseURL = 'http://localhost:5173/'
// axios.defaults.baseURL = 'http://localhost:8080/'
axios.defaults.baseURL = window.location.protocol + "//" + window.location.host + "/checkIn/api/";

//post请求头
axios.defaults.headers.post["Content-Type"] = "application/json;charset=UTF-8";
//允许跨域携带cookie信息
axios.defaults.withCredentials = true;
//设置超时
axios.defaults.timeout = 15000;

axios.interceptors.request.use(
    config => {
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

axios.interceptors.response.use(
    response => {
        if (response.status === 200) {
            return Promise.resolve(response.data);
        } else {
            return Promise.reject(response);
        }
    },
    error => {
        console.error(error);
        return Promise.reject(error)
    }
);
export default {
    /**
     * @param {String} url
     * @param {Object} data
     * @returns Promise
     */
    post(url, data) {
        return new Promise((resolve, reject) => {
            axios.post(url, data).then(
                    response => {
                        try {
                            resolve(response)
                        } catch (e) {
                            reject(response)
                        }
                    },
                    error => {
                        reject(error)
                    });
        })
    },
    get(url, data) {
        return axios.get(url, data);
    }
};