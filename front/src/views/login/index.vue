<template>
  <div class="login-container">
    <el-form class="login-form" autoComplete="on" :model="loginForm" :rules="loginRules" ref="loginForm" label-position="left">
      <div class="title-container">
        <h3 class="title">登录</h3>
      </div>
      <el-form-item prop="username">
        <span class="svg-container svg-container_login">
          <svg-icon icon-class="user" />
        </span>
        <el-input style=" background: transparent;" name="username" type="text" v-model="loginForm.username" autoComplete="on" placeholder="username" />
      </el-form-item>

      <el-form-item prop="password">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input  style=" background: transparent;" name="password" :type="passwordType" @keyup.enter.native="handleLogin" v-model="loginForm.password" autoComplete="on" placeholder="password" />
        <span class="show-pwd" @click="showPwd">
          <svg-icon icon-class="eye" />
        </span>
      </el-form-item>
      <el-button type="primary" style="width:100%;margin-bottom:30px;" :loading="loading" @click.native.prevent="handleLogin">确认</el-button>
    </el-form>
    <el-button class="thirdparty-button" type="primary"  @click="registerFlag=true">注册</el-button>
    <div class="md-modal modal-msg md-modal-transition" v-bind:class="{'md-show':registerFlag}">
      <div class="md-modal-inner">
        <div class="md-top">
          <div class="md-title">注册</div>
          <button class="md-close" @click="registerFlag=false">Close</button>
        </div>
        <div class="md-content">
          <div class="confirm-tips">
            <div class="error-wrap">
              <span class="error error-show" v-show="regErrorTip">密码错误</span>
            </div>
            <ul>
              <li class="regi_form_input">
                <input type="text" tabindex="1" name="loginname" v-model="regName" class="regi_login_input regi_login_input_left" placeholder="用户名" data-type="loginname">
              </li>
              <li class="regi_form_input noMargin">
                <input type="password" tabindex="2"  name="password" v-model="regPwd1" class="regi_login_input regi_login_input_left login-input-no input_text" placeholder="密码（不小于5位）" >
              </li>
              <li class="regi_form_input noMargin">
                <input type="password" tabindex="3"  name="password" v-model="regPwd2" class="regi_login_input regi_login_input_left login-input-no input_text" placeholder="确认密码" >
              </li>
            </ul>
          </div>
          <div class="login-wrap">
            <a href="javascript:;" class="btn-login" @click="register">注册</a>
          </div>
        </div>
      </div>
    </div>
    <div class="md-overlay" v-if="registerFlag" @click="registerFlag=false"></div>
  </div>
</template>
<style>
  .el-input input {
    background: transparent;
    border: 0px;
    -webkit-appearance: none;
    border-radius: 0px;
    padding: 12px 5px 12px 15px;
    color:#eee;
    height: 47px;
  }
  .el-input input :-webkit-autofill{
    -webkit-box-shadow: 0 0 0px 1000px #2d3a4b inset !important;
    -webkit-text-fill-color: #fff !important;
  }
</style>
<style rel="stylesheet/scss" lang="scss" scoped>
  $bg:#2d3a4b;
  $light_gray:#eee;
  /* reset element-ui css */
  .login-container {
    .el-input {
      display: inline-block;
      height: 47px;
      width: 85%;
      background: transparent;
      input {
        background: transparent;
        border: 0px;
        -webkit-appearance: none;
        border-radius: 0px;
        padding: 12px 5px 12px 15px;
        color: $light_gray;
        height: 47px;
        &:-webkit-autofill {
          -webkit-box-shadow: 0 0 0px 1000px $bg inset !important;
          -webkit-text-fill-color: #fff !important;
        }
      }
    }
    .el-form-item {
      border: 1px solid rgba(255, 255, 255, 0.1);
      background: rgba(0, 0, 0, 0.1);
      border-radius: 5px;
      color: #454545;
    }
  }
</style>
<style rel="stylesheet/scss" lang="scss" scoped>
  @import "../../assets/scss/mixin.scss";
  $bg:#2d3a4b;
  $dark_gray:#889aa4;
  $light_gray:#eee;
  .login-container {
   //@include relative;
    position:relative;
    top:0;
    height: 100vh;
    background-color: $bg;
    .login-form {
      position: absolute;
      left: 0;
      right: 0;
      width: 520px;
      padding: 35px 35px 15px 35px;
      margin: 120px auto;
    }
    .tips {
      font-size: 14px;
      color: #fff;
      margin-bottom: 10px;
      span {
        &:first-of-type {
          margin-right: 16px;
        }
      }
    }
    .svg-container {
      padding: 6px 5px 6px 15px;
      color: $dark_gray;
      vertical-align: middle;
      width: 30px;
      display: inline-block;
      &_login {
        font-size: 20px;
      }
    }
    .title-container {
      position: relative;
      .title {
        font-size: 26px;
        font-weight: 400;
        color: $light_gray;
        margin: 0px auto 40px auto;
        text-align: center;
        font-weight: bold;
      }
      .set-language {
        color: #fff;
        position: absolute;
        top: 5px;
        right: 0px;
      }
    }
    .show-pwd {
      position: absolute;
      right: 10px;
      top: 7px;
      font-size: 16px;
      color: $dark_gray;
      cursor: pointer;
      user-select: none;
    }
    .thirdparty-button {
      position: absolute;
      right: 35px;
      bottom: 28px;
    }
  }
</style>
<script>
  import { mapGetters } from 'vuex'
export default {
  name: 'login',
  data() {
    const validateUsername = (rule, value, callback) => {
      if (!(value)) {
        callback(new Error('Please enter the correct user name'))
      } else {
        callback()
      }
    }
    const validatePassword = (rule, value, callback) => {
      if (value.length < 6) {
        callback(new Error('The password can not be less than 6 digits'))
      } else {
        callback()
      }
    }
    return {
      loginForm: {
        username: 'admin',
        password: '123456',
      },
      loginRules: {
        username: [{ required: true, trigger: 'blur', validator: validateUsername }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }]
      },
      passwordType: 'password',
      loading: false,
      showDialog: false,
      registerFlag: false,
      regErrorTip:false,
      regName: 'admin',
      regPwd1: '123456',
      regPwd2: '123456',
    }
  },
  methods: {
    showPwd() {
      if (this.passwordType === 'password') {
        this.passwordType = ''
      } else {
        this.passwordType = 'password'
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          this.$store.dispatch('LoginByUsername', this.loginForm).then(() => {
            this.loading = false
            this.$router.push({ path: '/' })
          }).catch(() => {
            this.loading = false
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    register(){
      if(!this.regName || !this.regPwd1 ||!this.regPwd2||this.regPwd1!=this.regPwd2){
        this.regErrorTip=true;
        return;
      }
      let param=new URLSearchParams();
      param.append("username",this.regName);
      param.append("password",this.regPwd2);
      axios.post("/kjb/ums/register",param).then((response)=>{
        let res=response.data;
        if(res.status==1){
          this.regErrorTip=false;
          this.registerFlag=false;
          this.regSuc=true;
        }else{
          this.regErrorTip=true;
        }
      })

    }
  }
}
</script>


