package com.seizou.kojo.domain.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.seizou.kojo.domain.dto.Bfmk07Dto;
import com.seizou.kojo.domain.entity.Bfmk07Entity;
import com.seizou.kojo.domain.form.Bfmk07CheckForm;
import com.seizou.kojo.domain.form.Bfmk07Form;
import com.seizou.kojo.domain.service.Bfmk07Service;

/**
 * 部署情報のControllerクラス
 * @author T.Tateyama
 *
 */
@Controller
@RequestMapping(value = "b-forme_Kojo/pc")
public class Bfmk07Controller {

	@Autowired
	private Bfmk07Service service;

	/** １ページの表示数 */
	private final int MAX_SHOW_PAGE = 8;

	/** 現在ページ */
	private static int currentPage;

	/**
	* 初期設定
	* @param model
	* @return 画面
	*/
	@GetMapping(value = "/207")
	public String init(Model model) {

		// 適用日（FROM)を取得
		String today = service.today();

		// リスト総数取得
		int totalNum = service.countNum(); 
		int totalPage;

		// ページ総数の判定
		if(totalNum % MAX_SHOW_PAGE == 0) {
			totalPage = totalNum / MAX_SHOW_PAGE;
		}else {
			totalPage = totalNum / MAX_SHOW_PAGE + 1;
		}

		// 初期表示の場合、1ページに設定
		if (currentPage == 0){
			currentPage = 1;
		}

		// リスト情報を取得
		List<Bfmk07Entity> listAll = service.getDBList(currentPage, MAX_SHOW_PAGE);

		model.addAttribute("list", listAll);
		model.addAttribute("totalNum", totalNum);
		model.addAttribute("page", currentPage);
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("today", today);

		return "bfmk07View";
	}

	/**
	 * 戻る処理
	 * @param model
	 * @return メニュー画面
	 */
	@PostMapping(path = "/207", params = "back")
	public String back(Model model) {
		return "bfkt02View";
	}

	/**
	* クリア処理
	* @param model
	* @return 画面
	*/
	@PostMapping(path = "/207", params = "clrear")
	public String clear(Model model) {

		// 初期設定メソッドを呼び出し
		this.init(model);

		return "bfmk07View";
	}

	/**
	* 登録処理
	* @param 部署情報フォーム
	* @param モデル
	* @return 画面
	*/
	@PostMapping(path = "/207", params = "register")
	public String register(Bfmk07Form form, Model model) {

		// 検索項目の入力チェック
		String msg = service.formValidate(form);

		// 入力チェックでエラーがあった場合、取得したメッセージIDを設定
		if(!msg.isBlank()) {
			model.addAttribute("message", msg);

			// 初期設定メソッドを呼び出し
			this.init(model);

		// 入力チェックでエラー無しの場合
		}else {

			// 論理削除されたデータが存在するか確認
			String errMsg = service.flg1Check(form);

			// データが存在する場合（更新処理）
			if(!errMsg.equals("")) {
				String upResult = service.update(form);
				model.addAttribute("message", upResult);

			// 論理削除されたデータが存在しない場合（登録処理）
			}else {

				// 既に登録済みでないか確認
				String checkResult = service.flg0Check(form);

				// 登録があった場合、エラー
				if(!checkResult.equals("")) {
					model.addAttribute("message", checkResult);
				// 登録がない場合
				}else {

					// 登録
					String result = service.register(form);

					// 処理結果を設定
					model.addAttribute("message", result);
				}
			}

			// 初期設定メソッドを呼び出し
			this.init(model);
		}

		return "bfmk07View";
	}

	/**
	* チェックボックスの初期化
	* @return 画面
	*/
	@ModelAttribute(value = "checkForm")
	public Bfmk07CheckForm initBfmk07CheckForm() {

		return new Bfmk07CheckForm();
	}

	/**
	* 論理削除処理
	* @param checkForm
	* @param dto
	* @param model
	* @return 画面
	*/
	@PostMapping(path = "/207", params = "delete")
	public String delete(Bfmk07CheckForm checkForm, Bfmk07Dto dto, Model model) {

		// チェックボックスの確認
		String checkBoxFlg = service.checkValidate(checkForm);

		// チェックが無い場合、エラー
		if(!checkBoxFlg.isBlank()) {
			model.addAttribute("message", checkBoxFlg);

		// 所属人数の確認
		} else {
			String numResultFlg = service.checkNumber(checkForm);

			// 0人ではない場合
			if(!numResultFlg.isBlank()) {
				model.addAttribute("message", numResultFlg);

			}else {
				// チェックの件数分論理削除処理
				for(int i = 0; i < checkForm.getChecks().size(); i++) {
					String delResult = service.delete(checkForm.getChecks().get(i));

					// 処理結果を設定
					model.addAttribute("message", delResult);
				}
			}
		}

		// 初期設定メソッドを呼び出し
		this.init(model);

		return "bfmk07View";
	}

	/**
	*  ページネーション最初のページに戻る
	* @param params
	* @param model
	* @return 画面
	*/
	@PostMapping(path = "/207", params = "firstPage")
	public String firstPage(Bfmk07Form form, Model model) {

		// リスト総数の取得
		int totalNum = service.countNum(); 
		int totalPage;

		// ページ総数の判定
		if(totalNum % MAX_SHOW_PAGE == 0) {
			totalPage = totalNum / MAX_SHOW_PAGE;
		}else {
			totalPage = totalNum / MAX_SHOW_PAGE + 1;
		}

		currentPage = 1;

		// リスト情報の取得
		List<Bfmk07Entity> listAll = service.getDBList(currentPage, MAX_SHOW_PAGE);

		model.addAttribute("affilicate_id", form.getAffilicate_id());
		model.addAttribute("affilicate_name", form.getAffilicate_name());
		model.addAttribute("affilicate_name_r", form.getAffilicate_name_r());
		model.addAttribute("apply_strt_date", form.getApply_strt_date());
		model.addAttribute("apply_fin_date", form.getApply_fin_date());
		model.addAttribute("totalNum", totalNum);
		model.addAttribute("page", currentPage);
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("list", listAll);

		return "bfmk07View";
	}

	/**
	*  ページネーション前ページに戻る
	* @param model
	* @return 画面
	*/
	@PostMapping(path = "/207", params = "backPage")
	public String backPage(Bfmk07Form form, Model model) {

		// リスト総数取得
		int totalNum = service.countNum(); 
		int totalPage;

		// ページ総数の判定
		if(totalNum % MAX_SHOW_PAGE == 0) {
			totalPage = totalNum / MAX_SHOW_PAGE;
		}else {
			totalPage = totalNum / MAX_SHOW_PAGE + 1;
		}
	
		// 最初のページではない場合、１ページ分減算
		if (currentPage > 1) {
			currentPage--;
		}

		// リスト情報の取得
		List<Bfmk07Entity> listAll = service.getDBList(currentPage, MAX_SHOW_PAGE);
	
		model.addAttribute("affilicate_id", form.getAffilicate_id());
		model.addAttribute("affilicate_name", form.getAffilicate_name());
		model.addAttribute("affilicate_name_r", form.getAffilicate_name_r());
		model.addAttribute("apply_strt_date", form.getApply_strt_date());
		model.addAttribute("apply_fin_date", form.getApply_fin_date());
		model.addAttribute("totalNum", service.countNum());
		model.addAttribute("page", currentPage);
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("list", listAll);

		return "bfmk07View";
	}

	/**
	*  ページネーション次のページに進む
	* @param model
	* @return 画面
	*/
	@PostMapping(path = "/207", params = "nextPage")
	public String nextPage(Bfmk07Form form, Model model) {

		// リスト総数取得
		int totalNum = service.countNum(); 
		int totalPage;

		// ページ総数の判定
		if(totalNum % MAX_SHOW_PAGE == 0) {
			totalPage = totalNum / MAX_SHOW_PAGE;
		}else {
			totalPage = totalNum / MAX_SHOW_PAGE + 1;
		}

		// 最後のページではない場合、１ページ分加算
		if (currentPage < totalPage) {
			currentPage++;
		}

		// リスト情報の取得
		List<Bfmk07Entity> listAll = service.getDBList(currentPage, MAX_SHOW_PAGE);

		model.addAttribute("affilicate_id", form.getAffilicate_id());
		model.addAttribute("affilicate_name", form.getAffilicate_name());
		model.addAttribute("affilicate_name_r", form.getAffilicate_name_r());
		model.addAttribute("apply_strt_date", form.getApply_strt_date());
		model.addAttribute("apply_fin_date", form.getApply_fin_date());
		model.addAttribute("totalNum", totalNum);
		model.addAttribute("page", currentPage);
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("list", listAll);

		return "bfmk07View";
	}

	/**
	*  ページネーション最後のページに進む
	* @param model
	* @return 画面
	*/
	@PostMapping(path = "/207", params = "lastPage")
	public String lastPage(Bfmk07Form form, Model model) {

		// リスト総数の取得
		int totalNum = service.countNum(); 
		int totalPage;

		// ページ総数の判定
		if(totalNum % MAX_SHOW_PAGE == 0) {
			totalPage = totalNum / MAX_SHOW_PAGE;
		}else {
			totalPage = totalNum / MAX_SHOW_PAGE + 1;
		}

		// 現在ページを最後のページで保持
		currentPage = totalPage;

		// リスト情報の取得
		List<Bfmk07Entity> listAll = service.getDBList(currentPage, MAX_SHOW_PAGE);

		model.addAttribute("affilicate_id", form.getAffilicate_id());
		model.addAttribute("affilicate_name", form.getAffilicate_name());
		model.addAttribute("affilicate_name_r", form.getAffilicate_name_r());
		model.addAttribute("apply_strt_date", form.getApply_strt_date());
		model.addAttribute("apply_fin_date", form.getApply_fin_date());
		model.addAttribute("totalNum", totalNum);
		model.addAttribute("page", currentPage);
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("list", listAll);

		return "bfmk07View";
	}
}