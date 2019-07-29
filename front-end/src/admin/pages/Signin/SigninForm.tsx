import React, { Component } from "react";
import "../../../assets/scss/admin.scss";
import Header from "../../components/Header";
import * as userTypes from "../../../types/user";
import SigninForm from "../../../service/components/SigninForm";

interface Props {
  fetchSignin(payload: userTypes.IUserSigninInfo): void;
}
export interface State {
  id: string;
  password: string;
}

export default class AdminSignin extends Component<Props, State> {
  state = { id: "", password: "" };

  handleInputChange = (e: React.FormEvent<HTMLInputElement>) => {
    this.setState({
      [e.currentTarget.name]: e.currentTarget.value
    } as { [K in keyof State]: State[K] });
  };

  handleSigninSubmit = (e: React.FormEvent<HTMLInputElement>) => {
    const { id, password } = this.state;
    const { fetchSignin } = this.props;
    e.preventDefault();
    fetchSignin({ id: id, password: password });
  };

  render() {
    const { handleInputChange, handleSigninSubmit } = this;
    return (
      <>
        <Header />
        <main className="container">
          <div id="login-form" className="card">
            <h3>로그인</h3>
            <div className="input-group">
              <div className="input-group-prepend">
                <span className="input-group-text">아이디</span>
              </div>
              <input
                type="text"
                name="id"
                className="form-control"
                placeholder="아이디를 입력하세요."
                onChange={e => handleInputChange(e)}
              />
            </div>
            <div className="input-group">
              <div className="input-group-prepend">
                <span className="input-group-text">비밀번호</span>
              </div>
              <input
                type="password"
                name="password"
                className="form-control"
                placeholder="비밀번호를 입력하세요."
                onChange={e => handleInputChange(e)}
              />
            </div>
            <input type="submit" onClick={e => handleSigninSubmit(e)} value="로그인" />
          </div>
        </main>
      </>
    );
  }
}