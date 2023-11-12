import axios from 'axios';

const Server_IP = process.env.REACT_APP_Server_IP;

export const loginApi = async (provider, code) => {
    return await axios.post(`${Server_IP}/auth/${provider}`, { authCode: code })
    .then((res) => {
        if (res.data.token !== undefined) {
            console.log(res.data)
            alert(res.data)
            window.localStorage.setItem('refreshToken', res.data.refreshToken);
            return res.data.token;
        }
    })
    .catch((err) => {
        alert(err)
        console.log(err)
    })
}

export default loginApi;


