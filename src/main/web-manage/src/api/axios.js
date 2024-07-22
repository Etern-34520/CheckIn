import axios from "axios";
import {ElMessage} from "element-plus";

// axios.defaults.baseURL = 'http://localhost:5173/'
// axios.defaults.baseURL = 'http://localhost:8080/'
// axios.defaults.baseURL = window.location.host;

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
            return Promise.resolve(response);
        } else {
            return Promise.reject(response);
        }
    },
    error => {
        /*
                alert(JSON.stringify(error), '请求异常', {
                    confirmButtonText: '确定',
                    callback: (action) => {
                        console.log(action)
                    }
                });
        */
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
                            resolve(response.data)
                        } catch (e) {
                            reject(response)
                        }
                    },
                    error => {
                        reject(err)
                    });
        })
    },
    get(url, data) {
        return new Promise((resolve, reject) => {
            axios.get(url, data).then(
                response => {
                    resolve(response.data)
                },
                error => {
                    reject(err)
                });
        })
    }
};