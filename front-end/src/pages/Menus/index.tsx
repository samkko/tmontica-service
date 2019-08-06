import * as React from "react";
import "./styles.scss";
import { RouteComponentProps } from "react-router-dom";
import { getMenuAll } from "../../api/menu";
import MenuItems from "../../components/MenusItems";
import { IMenuItemsProps } from "../../components/MenusItems";
import { CommonError } from "../../api/CommonError";

interface MatchParams {
  categoryEng: string;
}

interface IMenusProps extends RouteComponentProps<MatchParams> {}

interface IMenusState {
  menuAll: Object;
}

export default class Menus extends React.Component<IMenusProps, IMenusState> {
  state = {
    menuAll: []
  };

  async getMenuAll() {
    try {
      const menuAll = await getMenuAll();
      if (menuAll instanceof CommonError) {
        throw menuAll;
      }

      if (!menuAll) throw new Error("메뉴 정보가 없습니다.");
      this.setState({
        menuAll
      });
    } catch (error) {
      if (!error.status) {
        alert("네트워크 오류 발생");
        return;
      }

      error.alertMessage();
    }
  }

  componentDidMount() {
    this.getMenuAll();
  }

  render() {
    return (
      <>
        <main className="main">
          <section className="banner">
            {<img src="" alt="Banner" className="banner__img" />}
          </section>

          {this.state.menuAll
            ? Array.from(this.state.menuAll).map((menu: IMenuItemsProps, i: number) => (
                <MenuItems
                  key={i}
                  categoryKo={menu.categoryKo}
                  categoryEng={menu.categoryEng}
                  menus={menu.menus}
                />
              ))
            : "Loading..."}
        </main>
        <footer className="footer">
          <div className="footer__container" />
        </footer>
      </>
    );
  }
}